package ch.epfl.sdp.social.friends_firestore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.social.User;

public class FirestoreFriendsFetcher extends RemoteFriendFetcher {

    @Override
    public void getFriendsFromServer(String constraint) {
        if (constraint == null) return;
        List<User> filtered = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                for (DocumentSnapshot doc: docs)
                {
                    if (doc.getId().contains(constraint))
                    {
                        filtered.add(new User(doc.getId(), (String)doc.getData().get("username")));
                    }
                }
                waiter.signalFriendsFetched(filtered);

            }
        });

    }
}
