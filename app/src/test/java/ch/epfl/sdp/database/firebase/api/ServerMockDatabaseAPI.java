package ch.epfl.sdp.database.firebase.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.epfl.sdp.database.firebase.entityForFirebase.EnemyForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.ItemBoxForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.ItemsForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.PlayerForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.UserForFirebase;
import ch.epfl.sdp.utils.CustomResult;
import ch.epfl.sdp.utils.OnValueReadyCallback;
import ch.epfl.sdp.geometry.area.Area;

public class ServerMockDatabaseAPI implements ServerDatabaseAPI {
    private Map<String, UserForFirebase> userForFirebaseMap = new HashMap<>();
    public Map<String, PlayerForFirebase> playerForFirebaseMap = new HashMap<>();
    private Map<String, ItemsForFirebase> usedItems = new HashMap<>();
    public Map<String, ItemsForFirebase> items = new HashMap<>();

    public ServerMockDatabaseAPI() {

    }

    public void hardCodedInit(Map<String, UserForFirebase> userForFirebaseMap, Map<String, PlayerForFirebase> playerForFirebaseMap, Map<String, ItemsForFirebase> usedItems, Map<String, ItemsForFirebase> items){
        // populate the all Users in firebase
        this.userForFirebaseMap = userForFirebaseMap;

        // populate the Players in lobby
        this.playerForFirebaseMap = playerForFirebaseMap;

        // populate the Enemy

        // populate the itemBox

        // populate the usedItem for each player in the lobby
        this.usedItems = usedItems;

        // populate the owned items for each player in the lobby
        this.items = items;
    }

    @Override
    public void setLobbyRef(String lobbyName) {

    }

    @Override
    public void listenToNumOfPlayers(OnValueReadyCallback<CustomResult<Void>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(null, true, null));
    }

    @Override
    public void startGame(OnValueReadyCallback<CustomResult<Void>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(null, true, null));
    }

    @Override
    public void fetchGeneralScoreForPlayers(List<String> playerEmailList, OnValueReadyCallback<CustomResult<List<UserForFirebase>>> onValueReadyCallback) {
        List<UserForFirebase> userForFirebaseList = new ArrayList<>();
        for(String email: playerEmailList) {
            userForFirebaseList.add(userForFirebaseMap.get(email));
        }
        onValueReadyCallback.finish(new CustomResult<>(userForFirebaseList, true, null));
    }

    @Override
    public void sendEnemies(List<EnemyForFirebase> enemies) {

    }

    @Override
    public <T> void addCollectionListener(Class<T> tClass, String collectionName, OnValueReadyCallback<CustomResult<List<T>>> onValueReadyCallback) {
        if (tClass == PlayerForFirebase.class) {
            onValueReadyCallback.finish(new CustomResult<>(new ArrayList<>((Collection<? extends T>) playerForFirebaseMap.values()), true, null));
        }
    }

    @Override
    public void sendItemBoxes(List<ItemBoxForFirebase> itemBoxForFirebaseList) {

    }

    @Override
    public void sendPlayersStatus(List<PlayerForFirebase> playerForFirebaseList) {
        for (PlayerForFirebase playerForFirebase: playerForFirebaseList){
            PlayerForFirebase player = playerForFirebaseMap.get(playerForFirebase.getEmail());
            Objects.requireNonNull(player).setHealthPoints(playerForFirebase.getHealthPoints());
            player.setAoeRadius(playerForFirebase.getAoeRadius());
            player.setPhantom(playerForFirebase.isPhantom());
        }
    }

    @Override
    public void cleanListeners() {

    }

    @Override
    public void sendServerAliveSignal(long signal) {

    }

    @Override
    public void updatePlayersCurrentScore(Map<String, Integer> emailsScoreMap) {

    }

    @Override
    public void sendPlayersItems(Map<String, ItemsForFirebase> emailsItemsMap) {
        for (Map.Entry<String, ItemsForFirebase> item: emailsItemsMap.entrySet()) {
            items.put(item.getKey(),item.getValue());
        }
    }

    @Override
    public void addUsedItemsListener(OnValueReadyCallback<CustomResult<Map<String, ItemsForFirebase>>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(usedItems, true, null));
    }

    @Override
    public void sendGameArea(Area gameArea) {

    }
}
