package ch.epfl.sdp.database.firebase;

import java.util.concurrent.CompletableFuture;

import ch.epfl.sdp.database.room.LeaderoardViewModel;
import ch.epfl.sdp.entity.Player;

public interface CommonDatabaseAPI {

    void syncCloudFirebaseToRoom(LeaderoardViewModel leaderoardViewModel);

    void addUser(UserForFirebase userForFirebase, OnAddUserCallback onAddUserCallback);

    void fetchUser(String email, OnFetchedCallback<UserForFirebase> onFetchedCallback);

    void joinLobby(PlayerForFirebase playerForFirebase);

    void fetchPlayers();
}
