package ch.epfl.sdp.ui.social;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sdp.R;
import ch.epfl.sdp.utils.MyApplication;
import ch.epfl.sdp.utils.WaitsOnWithServer;
import ch.epfl.sdp.social.conversation.RemoteToSQLiteAdapter;
import ch.epfl.sdp.social.conversation.SocialRepository;
import ch.epfl.sdp.database.room.social.Chat;
import ch.epfl.sdp.database.room.social.Message;

/**
 * This activity shows the conversation of the current user and another user
 */
public class ChatActivity extends AppCompatActivity implements WaitsOnWithServer<Message> {

    private EditText message;
    private String chattingWith;
    private Chat chatFromCurrent;
    private Chat chatFromFriend;
    private List<Message> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private RemoteToSQLiteAdapter sqliteFirestoreInterface;
    private String currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton sendButton = findViewById(R.id.sendMessageButton);
        message = findViewById(R.id.messageField);
        ListView lv = findViewById(R.id.messages_view);

        chattingWith = getIntent().getStringExtra("chattingWith");
        if (chattingWith == null) {
            // instrumentation test running so initialize to sentinel value "null_0"
            chattingWith = "null_0";
        }

        messageAdapter = new MessageAdapter(this, chattingWith);
        lv.setAdapter(messageAdapter);

        currentEmail = ((MyApplication) getApplication()).appContainer.authenticationAPI.getCurrentUserEmail();

        SocialRepository.setContextActivity(this);

        // The current user is the sender
        chatFromCurrent = SocialRepository.getInstance().getChat(currentEmail, chattingWith);

        // The current user is the receiver
        chatFromFriend = SocialRepository.getInstance().getChat(chattingWith, currentEmail);

        sendButton.setOnClickListener(this::onSendClicked);
        sqliteFirestoreInterface = ((MyApplication) getApplication()).appContainer.remoteToSQLiteAdapter;
        loadExistingMessages();
    }

    private void loadExistingMessages() {
        SocialRepository chatRepo = SocialRepository.getInstance();
        chatRepo.getMessagesExchanged(currentEmail, chattingWith);
        chatRepo.getMessagesExchanged(chattingWith, currentEmail);
        sqliteFirestoreInterface.setListener(this);
        sqliteFirestoreInterface.sendRemoteServerDataToLocal(currentEmail, chattingWith, chatFromFriend.getChat_id());
    }

    /**
     * This method tells what do do when the send button is clicked
     * It sends the message the user has written
     *
     * @param v the view on which we clicked
     */
    public void onSendClicked(View v) {
        Message m = new Message(new Date(), message.getText().toString(), chatFromCurrent.getChat_id());
        messageAdapter.add(new MessageDecorator(m, false));
        SocialRepository chatRepo = SocialRepository.getInstance();
        chatRepo.storeMessage(m);
        sqliteFirestoreInterface.sendLocalDataToRemoteServer(currentEmail, chattingWith, m);
    }

    /**
     * Decorates a message with a flag that indicates whether the message was incoming or outgoing
     */
    static final class MessageDecorator {
        private final Message m;
        private final boolean incoming;

        MessageDecorator(Message m, boolean incoming) {
            this.m = m;
            this.incoming = incoming;
        }

        Message getM() {
            return m;
        }

        boolean isIncoming() {
            return incoming;
        }
    }

    @Override
    public void contentFetchedWithServer(List<Message> output, boolean isFromServer, boolean incoming) {
        List<String> texts = new ArrayList<>();
        for (Message m : output) {
            texts.add(m.getText());
        }
        messages = output;
        for (Message el : messages) {
            if (isFromServer) {
                SocialRepository.getInstance().insertMessageFromRemote(new Timestamp(el.getDate()), el.getText(), chatFromFriend.getChat_id());
            }
            messageAdapter.add(new MessageDecorator(el, incoming));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * needed for testing, returns the list of messages
     */
    public List<Message> getMessages() {
        if (messages == null) return null;
        return new ArrayList<>(messages);
    }
}
