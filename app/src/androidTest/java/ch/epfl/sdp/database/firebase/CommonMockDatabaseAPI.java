package ch.epfl.sdp.database.firebase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.database.firebase.api.CommonDatabaseAPI;
import ch.epfl.sdp.database.firebase.entityForFirebase.PlayerForFirebase;
import ch.epfl.sdp.database.firebase.entityForFirebase.UserForFirebase;
import ch.epfl.sdp.utils.CustomResult;
import ch.epfl.sdp.utils.OnValueReadyCallback;
import ch.epfl.sdp.entities.player.Player;
import ch.epfl.sdp.entities.player.PlayerManager;

public class CommonMockDatabaseAPI implements CommonDatabaseAPI {
    private final HashMap<String, UserForFirebase> userData;
    private final List<UserForFirebase> userForFirebaseList;

    public CommonMockDatabaseAPI(HashMap<String, UserForFirebase> userData, List<UserForFirebase> userForFirebaseList) {
        this.userData = userData;
        this.userForFirebaseList = userForFirebaseList;
    }

    @Override
    public void addUser(UserForFirebase userForFirebase, OnValueReadyCallback<CustomResult<Void>> OnValueReadyCallback) {
        if(userData.containsKey(userForFirebase.getEmail())) {
            OnValueReadyCallback.finish(new CustomResult<>(null, false, new IllegalArgumentException("Already exist!")));
        } else {
            userData.put(userForFirebase.getEmail(), userForFirebase);
            OnValueReadyCallback.finish(new CustomResult<>(null, true, null));
        }
    }

    @Override
    public void fetchUser(String email, OnValueReadyCallback<CustomResult<UserForFirebase>> onValueReadyCallback) {
        if(!userData.containsKey(email)) {
            onValueReadyCallback.finish(new CustomResult<>(null, false, new IllegalArgumentException("Not exists!")));
        } else {
            onValueReadyCallback.finish(new CustomResult<>(userData.get(email), true, null));
        }
    }

    @Override
    public void selectLobby(OnValueReadyCallback<CustomResult<Void>> onValueReadyCallback) {
        PlayerManager.getInstance().setNumPlayersInLobby(0);
        PlayerManager.getInstance().setIsServer(true);
        onValueReadyCallback.finish(new CustomResult<>(null, true, null));
    }

    @Override
    public void registerToLobby(PlayerForFirebase playerForFirebase, Map<String, Object> data, OnValueReadyCallback<CustomResult<Void>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(null, true, null));
    }

    @Override
    public void fetchPlayers(String lobbyName, OnValueReadyCallback<CustomResult<List<PlayerForFirebase>>> onValueReadyCallback) {

    }

    @Override
    public void generalGameScoreListener(OnValueReadyCallback<CustomResult<List<UserForFirebase>>> onValueReadyCallback) {
        onValueReadyCallback.finish(new CustomResult<>(userForFirebaseList, true, null));
    }

    @Override
    public void sendUserPosition(PlayerForFirebase playerForFirebase) {

    }

    @Override
    public void cleanListeners() {

    }

    @Override
    public void updatePlayerGeneralScore(Player player) {

    }
}
