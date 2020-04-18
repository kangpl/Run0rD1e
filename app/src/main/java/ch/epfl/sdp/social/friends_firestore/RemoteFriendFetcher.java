package ch.epfl.sdp.social.friends_firestore;


import ch.epfl.sdp.social.User;
import ch.epfl.sdp.social.WaitsOn;

/*
This interface provides an abstraction for class that fetch all friends from the remote server
 */
public interface RemoteFriendFetcher {
    void getFriendsFromServer(String constraint, WaitsOn<User> waiter);
}
