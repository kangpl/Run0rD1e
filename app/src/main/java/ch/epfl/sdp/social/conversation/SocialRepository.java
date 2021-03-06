package ch.epfl.sdp.social.conversation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import androidx.room.Room;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.epfl.sdp.utils.WaitsOn;
import ch.epfl.sdp.utils.WaitsOnWithServer;
import ch.epfl.sdp.database.room.social.Chat;
import ch.epfl.sdp.database.room.social.ChatDatabase;
import ch.epfl.sdp.database.room.social.IsFriendsWith;
import ch.epfl.sdp.database.room.social.Message;
import ch.epfl.sdp.database.room.social.User;

/**
 * Provides higher level abstraction of the database of the chat and models the database memory management of that database
 * Any modification to (or request from) the database storing the chat should be routed to the singleton instance of this class
 */
public final class SocialRepository {

    private final ChatDatabase chatDB;
    private Context contextActivity;
    private static boolean singletonCreated = false;
    @SuppressLint("StaticFieldLeak")
    private static SocialRepository singleton;
    public static String currentEmail;

    private SocialRepository(Context contextActivity) {
        chatDB = Room.databaseBuilder(contextActivity, ChatDatabase.class, "ChatDatabase").allowMainThreadQueries().build();
        this.contextActivity = contextActivity;
    }

    /**
     * Returns the chat repository instance which abstracts all direct SQL calls relating to the local chat database
     */
    public static SocialRepository getInstance() {
        return singleton;
    }

    /**
     * Sets the UI context of the chat repository
     *
     * @param contextActivity the UI context (typically an instance of the Activity class)
     */
    public static void setContextActivity(Context contextActivity) {
        if (!singletonCreated) {
            singleton = new SocialRepository(contextActivity);
            singletonCreated = true;
        }
        singleton.contextActivity = contextActivity;
    }

    /**
     * Stores a message in the local database of the chat
     *
     * @param message the message of the chat, the chat being uniquely identified by the tuple (from (sender), to (receiver))
     */
    @SuppressLint("StaticFieldLeak")
    public void storeMessage(final Message message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    singleton.chatDB.daoAccess().sendMessage(message);
                } catch (SQLiteConstraintException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }

    /**
     * Adds a chat instance to the local database (typically because the user has initiated a chat with a new friend)
     *
     * @param c the chat to be added to the database
     */
    @SuppressLint("StaticFieldLeak")
    public void addChat(final Chat c) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    singleton.chatDB.daoAccess().addChat(c);
                } catch (SQLiteConstraintException e) {
                    // already added, do nothing
                }
                return null;
            }

        }.execute();
    }

    /**
     * Adds a user to the chat database
     *
     * @param usr the user to be added to the database (this user can be the current user or the friend of the current user)
     */
    @SuppressLint("StaticFieldLeak")
    public void addUser(final User usr) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    singleton.chatDB.daoAccess().addUser(usr);
                } catch (SQLiteConstraintException e) {
                    //User already added to the database, do nothing
                }
                return null;
            }
        }.execute();
    }

    /**
     * Fetches the friend of User user from the local database
     *
     * @param user the current user to get the friends of
     */
    @SuppressLint("StaticFieldLeak")
    public void fetchFriends(final User user) {
        new AsyncTask<Void, Void, List<User>>() {
            private Context context;

            @SuppressLint("StaticFieldLeak")
            @Override
            protected List<User> doInBackground(Void... voids) {
                context = singleton.contextActivity;
                try {
                    return singleton.chatDB.daoAccess().areFriends(user.getEmail());
                } catch (Exception e) {
                    return new ArrayList<>();
                }

            }

            @Override
            protected void onPostExecute(List<User> friends) {
                ((WaitsOn<User>) (context)).contentFetched(friends);
            }
        }.execute();
    }

    /**
     * Marks user1 and user2 friends in the chat (local) database
     *
     * @param user1 the first user in the friends relation
     * @param user2 the second user
     */
    @SuppressLint("StaticFieldLeak")
    public void addFriends(final User user1, final User user2) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    singleton.chatDB.daoAccess().addFriendship(new IsFriendsWith(user1.getEmail(), user2.getEmail()));
                    singleton.chatDB.daoAccess().addFriendship(new IsFriendsWith(user2.getEmail(), user1.getEmail())); // friendship is symmetric
                } catch (SQLiteConstraintException e) {
                    // foreign key was not found so add both users to user db (done in caller)
                }
                return null;
            }
        }.execute();
    }

    /**
     * Gets the messages received
     *
     * @param sender   the Id of the owner of the messages (i.e the one who sent it)
     * @param receiver the Id of the receiver of the messages
     */
    @SuppressLint("StaticFieldLeak")
    public void getMessagesExchanged(final String sender, final String receiver) {

        new AsyncTask<Void, Void, List<Message>>() {

            @Override
            protected List<Message> doInBackground(Void... voids) {
                return new LinkedList<>(singleton.chatDB.daoAccess().getMessages(receiver, sender));
            }

            @Override
            protected void onPostExecute(List<Message> ls) {
                boolean incoming = true;
                if (sender.equals(currentEmail)) {
                    incoming = false;
                }
                ((WaitsOnWithServer<Message>) singleton.contextActivity).contentFetchedWithServer(ls, false, incoming);
            }
        }.execute();

    }

    /**
     * Gets the messages from the server and puts them in the local database (specifics of the server and how to fetch from it are handled from the activity)
     *
     * @param tm      the time at which the message was sent (obtained from the server)
     * @param content the content of the chat message
     * @param chat_id the id of the chat uniquely identified by sender and a receiver ids (respectively)
     */
    @SuppressLint("StaticFieldLeak")
    public void insertMessageFromRemote(Timestamp tm, String content, int chat_id) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    singleton.chatDB.daoAccess().sendMessage(new Message(tm.toDate(), content, chat_id));
                } catch (SQLiteConstraintException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    /**
     * Gets the conversation from "current" to "other" from local database (note that messages are aware of their parent chat but the chat is not aware of its child messages)
     *
     * @param current the id of the current user
     * @param other   the id of the friend of the current user
     * @return the chat instance
     */
    @SuppressLint("StaticFieldLeak")
    public Chat getChat(String current, String other) {
        List<Chat> output = null;
        try {
            output = new AsyncTask<Void, Void, List<Chat>>() {

                @Override
                protected List<Chat> doInBackground(Void... voids) {
                    return singleton.chatDB.daoAccess().getChatFromCurrentToOther(current, other);
                }

            }.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // return sentinel value new Chat("null_0", "null_1") in case nothing found (useful for testing to avoid exceptions)
        return output.isEmpty() ? new Chat("null_0", "null_1") : output.get(0);
    }
}