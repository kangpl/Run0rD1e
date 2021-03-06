package ch.epfl.sdp.database.firebase.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.database.firebase.entityForFirebase.EnemyForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.ItemBoxForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.ItemsForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.PlayerForFirebase;
import ch.epfl.sdp.utils.CustomResult;
import ch.epfl.sdp.utils.OnValueReadyCallback;
import ch.epfl.sdp.entities.player.PlayerManager;
import ch.epfl.sdp.items.item_box.ItemBoxManager;

public class ClientMockDatabaseAPI implements ClientDatabaseAPI {
    private Map<String, PlayerForFirebase> playerForFirebaseMap = new HashMap<>();
    private List<EnemyForFirebase> enemyForFirebasesList = new ArrayList<>();
    private List<ItemBoxForFirebase> itemBoxForFirebaseList = new ArrayList<>();
    private ItemsForFirebase userItems;
    private String gameArea;

    public void hardCodedInit(Map<String, PlayerForFirebase> playerForFirebaseMap, List<EnemyForFirebase> enemyForFirebasesList, List<ItemBoxForFirebase> itemBoxForFirebaseList, ItemsForFirebase userItems, String gameArea) {
        // populate the all Users in firebase

        // populate the Players in lobby
        this.playerForFirebaseMap = playerForFirebaseMap;

        // populate the Enemy
        this.enemyForFirebasesList = enemyForFirebasesList;

        // populate the itemBox
        this.itemBoxForFirebaseList = itemBoxForFirebaseList;

        // populate the usedItem
        this.userItems = userItems;

        // set the game area
        this.gameArea = gameArea;
    }

    @Override
    public void setLobbyRef(String lobbyName) {

    }

    @Override
    public void listenToGameStart(OnValueReadyCallback<CustomResult<Void>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(null, true, null));
    }

    @Override
    public <T> void addCollectionListener(Class<T> tClass, String collectionName, OnValueReadyCallback<CustomResult<List<T>>> onValueReadyCallback) {
        List<Object> entityList = new ArrayList<>();
        switch (collectionName) {
            case PlayerManager.ENEMY_COLLECTION_NAME:
                entityList.addAll(enemyForFirebasesList);
                break;
            case PlayerManager.PLAYER_COLLECTION_NAME:
                entityList.addAll(playerForFirebaseMap.values());
                break;
            case ItemBoxManager.ITEMBOX_COLLECTION_NAME:
                entityList.addAll(itemBoxForFirebaseList);
                break;
        }
        onValueReadyCallback.finish(new CustomResult<>((List<T>) entityList, true, null));
    }

    @Override
    public void addUserItemListener(OnValueReadyCallback<CustomResult<Map<String, Integer>>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(userItems.getItemsMap(), true, null));
    }

    @Override
    public void addGameAreaListener(OnValueReadyCallback<CustomResult<String>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(gameArea, true, null));
    }

    @Override
    public void sendUsedItems(ItemsForFirebase itemsForFirebase) {
    }

    @Override
    public void cleanListeners() {

    }

    @Override
    public void addServerAliveSignalListener(OnValueReadyCallback<CustomResult<Long>> onValueReadyCallback) {

    }
}
