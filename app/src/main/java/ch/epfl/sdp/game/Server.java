package ch.epfl.sdp.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.artificial_intelligence.RandomEnemyGenerator;
import ch.epfl.sdp.artificial_intelligence.SinusoidalMovement;
import ch.epfl.sdp.database.firebase.api.CommonDatabaseAPI;
import ch.epfl.sdp.database.firebase.api.ServerDatabaseAPI;
import ch.epfl.sdp.database.firebase.entity.EntityConverter;
import ch.epfl.sdp.database.firebase.entity.ItemsForFirebase;
import ch.epfl.sdp.database.firebase.entity.PlayerForFirebase;
import ch.epfl.sdp.database.firebase.entity.UserForFirebase;
import ch.epfl.sdp.entity.Enemy;
import ch.epfl.sdp.entity.EnemyManager;
import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.geometry.Area;
import ch.epfl.sdp.geometry.CircleArea;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.geometry.UnboundedArea;
import ch.epfl.sdp.item.Coin;
import ch.epfl.sdp.item.Healthpack;
import ch.epfl.sdp.item.ItemBox;
import ch.epfl.sdp.item.ItemBoxManager;
import ch.epfl.sdp.item.ItemFactory;

/**
 * Takes care of all actions that a server should perform (generating enemies, updating enemies etc.).
 */
public class Server implements Updatable {
    private static final String TAG = "Database";
    private int counter;
    private int scoreTimeCounter;
    private boolean gameEnd;
    private ServerDatabaseAPI serverDatabaseAPI;
    private CommonDatabaseAPI commonDatabaseAPI;
    private PlayerManager playerManager = PlayerManager.getInstance();
    private EnemyManager enemyManager = EnemyManager.getInstance();
    private ItemBoxManager itemBoxManager = ItemBoxManager.getInstance();
    private ItemFactory itemFactory;
    private Area gameArea;

    public Server(ServerDatabaseAPI serverDatabaseAPI, CommonDatabaseAPI commonDatabaseAPI) {
        this.serverDatabaseAPI = serverDatabaseAPI;
        this.commonDatabaseAPI = commonDatabaseAPI;
        this.counter = 0;
        this.scoreTimeCounter = 0;
        this.gameEnd = false;
        itemFactory = new ItemFactory();
    }

    @Override
    public void update() {
        if (counter <= 0) {
            sendGameArea();
            sendEnemies();
            sendItemBoxes();
            sendPlayersHealth();
            sendPlayersItems();
            checkPlayerStatus();
            counter = 2 * GameThread.FPS + 1;
        }

        --counter;

        // update Players score every 10 seconds
        if (scoreTimeCounter <= 0) {
            Log.d(TAG, "update: update the score of player");
            updateInGameScoreOfPlayer();
            scoreTimeCounter = 10 * GameThread.FPS + 1;
        }
        --scoreTimeCounter;

    }

    private void sendGameArea() {
        serverDatabaseAPI.sendGameArea(gameArea);
    }

    public void start() {
        serverDatabaseAPI.listenToNumOfPlayers(value -> {
            if (value.isSuccessful()) {
                Log.d(TAG, "initEnvironment: listenToNumberOf Players success");
                fetchPlayers();
            } else Log.d(TAG, "initEnvironment: failed" + value.getException().getMessage());
        });
    }

    private void fetchPlayers() {
        commonDatabaseAPI.fetchPlayers(playerManager.getLobbyDocumentName(), value1 -> {
            if (value1.isSuccessful()) {
                for (PlayerForFirebase playerForFirebase : value1.getResult()) {
                    Player player = EntityConverter.playerForFirebaseToPlayer(playerForFirebase);
                    if (!playerManager.getCurrentUser().getEmail().equals(player.getEmail())) {
                        playerManager.addPlayer(player);
                    }
                    Log.d(TAG, "(Server) Getting Player: " + player);
                }
                List<String> playersEmailList = new ArrayList<>();
                playersEmailList.addAll(playerManager.getPlayersMap().keySet());
                fetchGeneralScore(playersEmailList);
            } else Log.d(TAG, "initEnvironment: failed" + value1.getException().getMessage());
        });
    }

    private void fetchGeneralScore(List<String> playersEmailList) {
        serverDatabaseAPI.fetchGeneralScoreForPlayers(playersEmailList, value -> {
            if (value.isSuccessful()) {
                for (UserForFirebase userForFirebase : value.getResult()) {
                    playerManager.getPlayersMap().get(userForFirebase.getEmail()).setGeneralScore(userForFirebase.getGeneralScore());
                    Log.d(TAG, "init environment: fetch general score " + userForFirebase.getUsername() + " with score " + userForFirebase.getGeneralScore());
                }
                initGameArea();
                initItemBoxes();
                initEnemies();
                initCoins();
                startGame();
            } else Log.d(TAG, "init environment: fetch general score failed " + value.getException().getMessage());
        });
    }

    private void startGame() {
        serverDatabaseAPI.startGame(value -> {
            if (value.isSuccessful()) {
                Game.getInstance().addToUpdateList(this);
                Game.getInstance().initGame();
                addPlayersPositionListener();
                addUsedItemsListener();
            } else Log.d(TAG, "initEnvironment: failed" + value.getException().getMessage());
        });
    }

    private void initGameArea() {
        //GameArea -----------------------------------------
        GeoPoint local = PlayerManager.getInstance().getCurrentUser().getLocation();
        gameArea = new CircleArea(3000, local);
        Game.getInstance().addToDisplayList(gameArea);
        Game.getInstance().addToUpdateList(gameArea);
        Game.getInstance().areaShrinker.setGameArea(gameArea);
    }

    private void initEnemies() {
        // Enemy -------------------------------------------
        Area area = new UnboundedArea();
        RandomEnemyGenerator randomEnemyGenerator = new RandomEnemyGenerator(gameArea, area);
        randomEnemyGenerator.setEnemyCreationTime(1000);
        randomEnemyGenerator.setMaxEnemies(10);
        randomEnemyGenerator.setMinDistanceFromEnemies(100);
        randomEnemyGenerator.setMinDistanceFromPlayers(100);
        randomEnemyGenerator.generateEnemy(100);
        Enemy enemy = randomEnemyGenerator.getEnemies().get(0);
        SinusoidalMovement movement = new SinusoidalMovement();
        movement.setAngleStep(0.1);
        movement.setAmplitude(10);
        enemy.setMovement(movement);
        enemyManager.updateEnemies(enemy);
        //  -------------------------------------------
    }

    private void initCoins() {
        int amount = 10;
        ArrayList<Coin> coins = Coin.generateCoinsAroundLocation(playerManager.getCurrentUser().getLocation(), amount);
        for (Coin c : coins) {
            Game.getInstance().addToDisplayList(c);
            Game.getInstance().addToUpdateList(c);
        }
    }

    private void initItemBoxes() {
        // ItemBox -------------------------------------------
        Healthpack healthpack = new Healthpack(10);
        ItemBox itemBox = new ItemBox(new GeoPoint(6.14, 46.22));
        itemBox.putItems(healthpack, 2);
        Game.getInstance().addToDisplayList(itemBox);
        Game.getInstance().addToUpdateList(itemBox);

        Healthpack healthpack1 = new Healthpack(10);
        ItemBox itemBox1 = new ItemBox(new GeoPoint(6.1488, 46.2125));
        itemBox1.putItems(healthpack1, 1);
        Game.getInstance().addToDisplayList(itemBox1);
        Game.getInstance().addToUpdateList(itemBox1);

        ItemBoxManager.getInstance().addItemBox(itemBox); // puts in waiting list
        ItemBoxManager.getInstance().addItemBox(itemBox1);
        //  -------------------------------------------
    }

    private void addUsedItemsListener() {
        serverDatabaseAPI.addUsedItemsListener(value -> {
            if (value.isSuccessful()) {
                for (Map.Entry<String, ItemsForFirebase> entry : value.getResult().entrySet()) {
                    String email = entry.getKey();
                    ItemsForFirebase itemsForFirebase = entry.getValue();

                    for (Map.Entry<String, Integer> items : itemsForFirebase.getItemsMap().entrySet()) {
                        int usedCount = items.getValue();
                        for (int i = 0; i < usedCount; i++) {
                            itemFactory.getItem(items.getKey()).useOn(playerManager.getPlayersMap().get(email));
                            playerManager.getPlayersMap().get(email).getInventory().removeItem(items.getKey());
                        }
                    }
                }
            }
        });
    }

    private void addPlayersPositionListener() {
        serverDatabaseAPI.addPlayersPositionListener(value -> {
            if (value.isSuccessful()) {
                for (PlayerForFirebase playerForFirebase : value.getResult()) {
                    Player player = playerManager.getPlayersMap().get(playerForFirebase.getEmail());
                    GeoPoint location = EntityConverter.geoPointForFirebaseToGeoPoint(playerForFirebase.getGeoPointForFirebase());

                    if (player.getLocation() != null) {
                        Log.d(TAG, "addPlayersPositionListener: before " + playerForFirebase.getUsername() + " " + player.getLocation().getLatitude() + " " + player.getLocation().getLongitude());
                        Log.d(TAG, "addPlayersPositionListener: after " + playerForFirebase.getUsername() + " " + location.getLatitude() + " " + location.getLongitude());
                        double traveledDistance = player.getLocation().distanceTo(location);
                        player.updateDistanceTraveled(traveledDistance);

                        // update the location of the player
                        player.setLocation(location);
                        Log.d(TAG, "addPlayersPositionListener: traveledDistance" + traveledDistance);
                    }
                }
            } else Log.w(TAG, "addPlayersPositionListener: failed", value.getException());
        });
    }

    private void sendEnemies() {
        serverDatabaseAPI.sendEnemies(EntityConverter.convertEnemyList(enemyManager.getEnemies()));
    }

    private void sendItemBoxes() {
        itemBoxManager.moveTakenItemBoxesToWaitingList();
        Map<String, ItemBox> itemBoxMap = itemBoxManager.getWaitingItemBoxes();

        if (!itemBoxMap.isEmpty()) {
            serverDatabaseAPI.sendItemBoxes(EntityConverter.convertItemBoxMap(itemBoxMap));
            itemBoxManager.clearWaitingItemBoxes();
        }
    }

    private void sendPlayersHealth() {
        List<Player> players = playerManager.getPlayersWaitingHealthPoint();
        if (!players.isEmpty()) {
            serverDatabaseAPI.sendPlayersHealth(EntityConverter.convertPlayerList(players));
            playerManager.getPlayersWaitingHealthPoint().clear();
        }
    }

    private void sendPlayersItems() {
        List<Player> players = playerManager.getPlayersWaitingItems();
        if (!players.isEmpty()) {
            Map<String, ItemsForFirebase> playersItemsMap = new HashMap<>();
            for (Player player : players) playersItemsMap.put(player.getEmail(), EntityConverter.convertItems(player.getInventory().getItems()));
            serverDatabaseAPI.sendPlayersItems(playersItemsMap);
            playerManager.getPlayersWaitingItems().clear();
        }
    }

    private void updateInGameScoreOfPlayer() {
        Map<String, Integer> emailsScoreMap = new HashMap<>();
        for (Player player : PlayerManager.getInstance().getPlayers()) {
            player.updateLocalScore();
            Log.d(TAG, "updateInGameScoreOfPlayer: " + player.getEmail() + " " + player.getCurrentGameScore());
            emailsScoreMap.put(player.getEmail(), player.getCurrentGameScore());
        }

        serverDatabaseAPI.updatePlayersScore("currentGameScore", emailsScoreMap);
    }

    private void checkPlayerStatus() {
        int numberOfPlayerAlive = 0;
        // check the number of players alive
        for (Player player : playerManager.getPlayers()) {
            if (player.getHealthPoints() > 0) {
                numberOfPlayerAlive += 1;
            }
        }
        updateTheGeneralScore(numberOfPlayerAlive);
    }

    private void updateTheGeneralScore(int numberOfPlayerAlive) {
        if (numberOfPlayerAlive == 0 && !gameEnd) {
            // update the general score of players
            Map<String, Integer> emailsScoreMap = new HashMap<>();
            for (Player player : playerManager.getPlayers()) {
                player.setGeneralScore(player.getGeneralScore() + player.getCurrentGameScore());
                emailsScoreMap.put(player.getEmail(), player.getGeneralScore());
            }
            serverDatabaseAPI.updatePlayersScore("generalScore", emailsScoreMap);
            gameEnd = true;
        }
    }
}