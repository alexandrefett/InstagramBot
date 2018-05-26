package service;

import com.fett.Response.StandardResponse;
import com.fett.Response.StatusResponse;
import com.fett.interceptor.ErrorInterceptor;
import com.fett.model.User;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.interceptor.UserAgentInterceptor;
import me.postaddict.instagram.scraper.interceptor.UserAgents;
import me.postaddict.instagram.scraper.model.Account;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class InstagramService{

    private Firestore db;
    private Instagram instagram;

    @Autowired
    public InstagramService() {

        try {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(loggingInterceptor)
                    .addInterceptor(new UserAgentInterceptor(UserAgents.WIN10_CHROME))
                    .addInterceptor(new ErrorInterceptor())
                    .cookieJar(new DefaultCookieJar(new CookieHashSet()))
                    .build();

            FileInputStream serviceAccount = new FileInputStream("instamanager-908a3-aab9f9f25fd5.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://instamanager-908a3.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            this.db = FirestoreClient.getFirestore();
            this.instagram = new Instagram(httpClient);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void basePage() throws IOException{
        instagram.basePage();
    }

    public void login(String username, String password) throws IOException{
        instagram.login(username,password);
    }

    public Account getAccountById(long id) throws IOException {
        return instagram.getAccountById(id);
    }

    public Account getAccountByUsername(String username) throws IOException {
        return instagram.getAccountByUsername(username);
    }

    public StandardResponse follow(String username){
        try {
            final Account a = getAccountByUsername(username);
            new Thread() {

                @Override
                public void run() {
                    try {
                        int i = 0;
                        final List<Account> f = instagram.getFollowers(a.getId(), 20).getNodes();
                        for (Account a : f) {
                            if (!a.getRequestedByViewer() && !a.getFollowedByViewer()) {
                                i++;
                                System.out.println("i=" + i + "  username: " + a.getUsername());
                                instagram.followAccount(a.getId());
                                addRequested(a);
                                sleep(5000);
                            }
                            if (i == 39) {
                                System.out.println("Paused for 1 hour.");
                                i = 0;
                                sleep(1000 * 60 * 60);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("  IOException");
                        try {
                            Thread.sleep(1000 * 60 * 60);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        System.out.println("  InterruptException");
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
            return new StandardResponse(StatusResponse.ERROR, e.getMessage());
        }
        return new StandardResponse(StatusResponse.SUCCESS, "Follow thread started");

    }
    public User userRegister(User user){
        ApiFuture<com.google.firestore.v1beta1.WriteResult> result = db.collection("profile").document(user.getUid()).set(user.toMap());
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
        ApiFuture<WriteResult> result = db.collection("requested").document(String.valueOf(b.getId())).set(map);
    }

}
