package ch.epfl.sdp.social;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sdp.database.authentication.MockAuthenticationAPI;
import ch.epfl.sdp.utils.AppContainer;
import ch.epfl.sdp.utils.MockServerToSQLiteAdapter;
import ch.epfl.sdp.utils.MyApplication;
import ch.epfl.sdp.ui.social.ChatActivity;
import ch.epfl.sdp.social.conversation.SocialRepository;
import ch.epfl.sdp.database.room.social.Chat;
import ch.epfl.sdp.database.room.social.Message;
import ch.epfl.sdp.database.room.social.User;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Tests conversation-relevant functionality provided by SocialRepository.java.
 * Note that the activity is itself not tested but is used to provide context needed by the database builder used inside SocialRepository
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SocialRepositoryChatTest {

    // The users to add to the database
    private final List<User> fantasticSix = asList(
            new User("amro@gmail.com"),
            new User("saoud@gmail.com"),
            new User("sacha@gmail.com"),
            new User("sen@gmail.com"),
            new User("peilin@gmail.com"),
            new User("rafael@gmail.com"));
    private String currentUserEmail;


    @Rule
    public ActivityTestRule<ChatActivity> mActivityTestRule = new ActivityTestRule<ChatActivity>(ChatActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            AppContainer appContainer = ((MyApplication) ApplicationProvider.getApplicationContext()).appContainer;
            appContainer.remoteToSQLiteAdapter = MockServerToSQLiteAdapter.getInstance();
            appContainer.remoteToSQLiteAdapter.setListener(mActivityTestRule.getActivity());
            appContainer.authenticationAPI = new MockAuthenticationAPI(null, "amro@gmail.com");
            currentUserEmail = appContainer.authenticationAPI.getCurrentUserEmail();
            SocialRepository.setContextActivity(ApplicationProvider.getApplicationContext());
            prepopulateDatabase();
        }
    };

    @Before
    public void setup() {
        SocialRepository.setContextActivity(mActivityTestRule.getActivity());
        SocialRepository.currentEmail = currentUserEmail;
    }

    private SocialRepository testRepo;

    public void prepopulateDatabase() {
        // Pre-populate the database with sample users
        prepopulateDatabaseWithUserRecords();

        // Create the chat for each pair of users and mark each pair as friends
        for (User x : fantasticSix) {
            for (User y : fantasticSix) {
                testRepo.addChat(new Chat(x.getEmail(), y.getEmail()));
                testRepo.addFriends(x, y);
            }
        }
    }

    public void prepopulateDatabaseWithUserRecords() {
        testRepo = SocialRepository.getInstance();
        for (User user : fantasticSix) {
            testRepo.addUser(user);
        }
    }

    @Test
    public void AmroCanSendSachaMessage() throws InterruptedException {
        // get the conversation from Amro to Sacha
        Chat c = testRepo.getChat(fantasticSix.get(0).getEmail(), fantasticSix.get(2).getEmail());

        // send a joke message (should be taken lightly of course)
        testRepo.storeMessage(new Message(new Date(), "This code kills me, kills me", c.getChat_id()));

        // Pretend storing the message from the db will take 1 second
        Thread.sleep(1000);

        // wait asynchronously until messages are fetched (these two calls should return the same thing)
        testRepo.getMessagesExchanged(fantasticSix.get(0).getEmail(), fantasticSix.get(2).getEmail());

        // Pretend fetching twice the message from the db will take 2 second
        Thread.sleep(2000);

        String result = mActivityTestRule.getActivity().getMessages().get(0).getText();
        assertEquals("This code kills me, kills me", result);
    }

    @Test
    public void SachaCanReceiveRemoteMessage() throws InterruptedException {
        Chat c = testRepo.getChat(fantasticSix.get(0).getEmail(), fantasticSix.get(2).getEmail());
        testRepo.insertMessageFromRemote(new Timestamp(new Date()), "Blessed", c.getChat_id());
        // pretend inserting will take 4 seconds
        Thread.sleep(4000);

        testRepo.getMessagesExchanged(fantasticSix.get(0).getEmail(), fantasticSix.get(2).getEmail());

        // pretend fetching will take 4 seconds
        Thread.sleep(4000);

        while (mActivityTestRule.getActivity().getMessages() == null) ;

        List<Message> msgs = mActivityTestRule.getActivity().getMessages();
        List<String> texts = new ArrayList<>();
        for (Message m : msgs) {
            texts.add(m.getText());
        }
        assertEquals("Blessed", texts.get(1));
    }
}
