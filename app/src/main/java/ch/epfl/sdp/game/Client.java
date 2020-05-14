package ch.epfl.sdp.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.database.firebase.api.ClientDatabaseAPI;
import ch.epfl.sdp.database.firebase.api.CommonDatabaseAPI;
import ch.epfl.sdp.database.firebase.entity.EnemyForFirebase;
import ch.epfl.sdp.database.firebase.entity.EntityConverter;
import ch.epfl.sdp.database.firebase.entity.ItemBoxForFirebase;
import ch.epfl.sdp.database.firebase.entity.PlayerForFirebase;
import ch.epfl.sdp.entity.Enemy;
import ch.epfl.sdp.entity.EnemyManager;
import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.item.ItemBox;
import ch.epfl.sdp.item.ItemBoxManager;

/**
 * This class updates the game from the client point of view. It fetches the data from firebase and
 * the data is updated by the server.
 */
public class Client implements Updatable {
    private static final String TAG = "Database";
    private int counter = 0;
    private ClientDatabaseAPI clientDatabaseAPI;
    private CommonDatabaseAPI commonDatabaseAPI;
    private PlayerManager playerManager = PlayerManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private ItemBoxManager itemBoxManager = ItemBoxManager.getInstance();

    /**
     * Creates a new client
     */
    public Client(ClientDatabaseAPI clientDatabaseAPI, CommonDatabaseAPI commonDatabaseAPI) {
        this.clientDatabaseAPI = clientDatabaseAPI;
        this.commonDatabaseAPI = commonDatabaseAPI;

        // TODO Add Listener for and Players
        init();
        addEnemyListener();
        addItemBoxesListener();
        addIngameScoreAndHealthPointListener();
        addUserItemListener();
    }

    @Override
    public void update() {
//        checkEndOfGame();
        if (counter <= 0) {
            sendUserPosition();
            sendUsedItems();
            counter = 2 * GameThread.FPS + 1;
        }

        --counter;
    }

    private void init() {
        clientDatabaseAPI.listenToGameStart(start -> {
            if (start.isSuccessful()) {
                commonDatabaseAPI.fetchPlayers(playerManager.getLobbyDocumentName(), value1 -> {
                    if (value1.isSuccessful()) {
                        for (PlayerForFirebase playerForFirebase : value1.getResult()) {
                            Player player = EntityConverter.playerForFirebaseToPlayer(playerForFirebase);
                            if (!playerManager.getCurrentUser().getEmail().equals(player.getEmail())) {
                                playerManager.addPlayer(player);
                            }
                            Log.d(TAG, "(Server) Getting Player: " + player);
                        }
                        Game.getInstance().addToUpdateList(this);
                        Game.getInstance().initGame();
                    } else Log.d(TAG, "initEnvironment: failed" + value1.getException().getMessage()); });
            }
        });
    }

    private void addEnemyListener() {
        clientDatabaseAPI.addCollectionListerner(EnemyForFirebase.class, value -> {
            if (value.isSuccessful()) {
                List<EnemyForFirebase> enemyForFirebaseList = new ArrayList<>();
                for (Object object : value.getResult()) {
                    enemyForFirebaseList.add((EnemyForFirebase) object);
                }
                for (Enemy enemy : EntityConverter.convertEnemyForFirebaseList(enemyForFirebaseList)) {
                    enemyManager.updateEnemies(enemy);
                }
            }
        });
    }

    private void addItemBoxesListener() {
        // Listen for Items
        clientDatabaseAPI.addCollectionListerner(ItemBoxForFirebase.class, value -> {
            if (value.isSuccessful()) {
                List<ItemBoxForFirebase> itemBoxForFirebaseList = new ArrayList<>();
                for (Object object : value.getResult()) {
                    itemBoxForFirebaseList.add((ItemBoxForFirebase) object);
                }
                for (ItemBoxForFirebase itemBoxForFirebase : itemBoxForFirebaseList) {
                    String id = itemBoxForFirebase.getId();
                    boolean taken = itemBoxForFirebase.isTaken();
                    GeoPoint location = itemBoxForFirebase.getLocation();

                    if (itemBoxManager.getItemBoxes().containsKey(id)) {
                        if (taken) {
                            Game.getInstance().removeFromDisplayList(itemBoxManager.getItemBoxes().get(id));
                            itemBoxManager.getItemBoxes().remove(id);
                        }
                    } else {
                        if (!taken) {
                            ItemBox itemBox = new ItemBox(location);
                            Game.getInstance().addToDisplayList(itemBox);
                            itemBoxManager.addItemBoxWithId(itemBox, id);
                        }
                    }
                    Log.d(TAG, "Listen for itemboxes: " + value.getResult());
                }
            } else {
                Log.w(TAG, "Listen for itemBoxes failed.", value.getException());
            }
        });
    }

    private void addIngameScoreAndHealthPointListener() {
        clientDatabaseAPI.addCollectionListerner(PlayerForFirebase.class, value -> {
            if (value.isSuccessful()) {
                for (Object object : value.getResult()) {
                    PlayerForFirebase playerForFirebase = (PlayerForFirebase) object;
                    Player player = playerManager.getPlayersMap().get(playerForFirebase.getEmail());
                    if (player != null) {
                        player.setCurrentGameScore(playerForFirebase.getCurrentGameScore());
                        player.setHealthPoints(playerForFirebase.getHealthPoints());
                    }
                    Log.d(TAG, "Listen for ingameScore: " + playerForFirebase.getUsername() + " " + playerForFirebase.getCurrentGameScore());
                }
            } else {
                Log.w(TAG, "Listen for ingameScore failed.", value.getException());
            }
        });
    }

    private void addUserItemListener() {
        // Listen for Items
        clientDatabaseAPI.addUserItemListener(value -> {
            if (value.isSuccessful()) {
                Map<String, Integer> items = new HashMap<>();
                for (Map.Entry<String, Integer> entry : value.getResult().entrySet()) {
                    items.put(entry.getKey(), entry.getValue());
                }
                playerManager.getCurrentUser().getInventory().setItems(items);
                Log.d(TAG, "Listen for items: " + value.getResult());
            } else {
                Log.w(TAG, "Listen for items failed.", value.getException());
            }
        });
    }


    private void sendUserPosition() {
        clientDatabaseAPI.sendUserPosition(EntityConverter.playerToPlayerForFirebase(PlayerManager.getInstance().getCurrentUser()));
    }

    private void sendUsedItems() {
        Map<String, Integer> usedItems = playerManager.getCurrentUser().getInventory().getUsedItems();
        if (!usedItems.isEmpty()) {
            clientDatabaseAPI.sendUsedItems(EntityConverter.convertItems(usedItems));
            playerManager.getCurrentUser().getInventory().clearUsedItems();
        }
    }

    private void checkEndOfGame() {
        if (playerManager.getCurrentUser().getHealthPoints() == 0) {
            Game.getInstance().clearGame();
            Game.getInstance().destroyGame();
        }
    }
}
