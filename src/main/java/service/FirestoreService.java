package service;

import com.fett.model.User;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firestore.v1beta1.WriteResult;
import me.postaddict.instagram.scraper.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class FirestoreService {
    private Firestore db;

    @Autowired
    public FirestoreService() {
        try {
            FileInputStream serviceAccount = new FileInputStream("instamanager-908a3-aab9f9f25fd5.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://instamanager-908a3.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public User userRegister(User user){
        ApiFuture<WriteResult> result = db.collection("profile").document(user.getUid()).set(user.toMap());
        return user;
    }

    public void addRequested(Account b){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", b.getId());
        map.put("username", b.getUsername());
        map.put("fullName", b.getFullName());
        map.put("followedByViewer", b.getFollowedByViewer());
        map.put("requestedByViewer", b.getRequestedByViewer());
        map.put("profilePictureUrl", b.getProfilePicUrl());
        map.put("followsViewer", b.getFollowsViewer());
        map.put("isVerified", b.getIsVerified());
        map.put("date", Calendar.getInstance().getTimeInMillis()*-1);
        ApiFuture<com.google.cloud.firestore.WriteResult> result = db.collection("requested").document(String.valueOf(b.getId())).set(map);

    }
}
