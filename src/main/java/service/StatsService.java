package service;

import com.fett.interceptor.ErrorInterceptor;
import com.fett.model.Profile;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.interceptor.UserAgentInterceptor;
import me.postaddict.instagram.scraper.interceptor.UserAgents;
import me.postaddict.instagram.scraper.model.Account;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class StatsService {

    Timer timer = new Timer ();
    TimerTask hourlyTask = new TimerTask () {
        @Override
        public void run () {
            updateStats();
        }
    };

    public void startStsts(){
        timer.schedule(hourlyTask, 0l, 1000*60*60);
    }

    private void updateStats(){
        try {
            List<Profile> list = getProfiles();
            for(Profile p:list) {
                try {
                    Account account = getAccount(p.getUsername(), p.getPassword());
                    saveStats(p, account);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }  catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private Account getAccount(String username, String password) throws IOException {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .addInterceptor(new UserAgentInterceptor(UserAgents.WIN10_CHROME))
                .addInterceptor(new ErrorInterceptor())
                .cookieJar(new DefaultCookieJar(new CookieHashSet()))
                .build();

        Instagram instagram = new Instagram(httpClient);
        instagram.basePage();
        instagram.login(username, password);
        return instagram.getAccountByUsername(username);
    }

    private List<Profile> getProfiles() throws ExecutionException, InterruptedException {
        List<Profile> list = new ArrayList<Profile>();
        ApiFuture<QuerySnapshot> query = FirestoreClient.getFirestore().collection("profile").get();
        QuerySnapshot snap = query.get();
        List<QueryDocumentSnapshot> documents = snap.getDocuments();
        for (DocumentSnapshot document : documents) {
            list.add(document.toObject(Profile.class));
        }
        return list;
    }

    private void saveStats(Profile profile, Account account){
        System.out.println(profile.toMap());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("date", Calendar.getInstance().getTimeInMillis());
        map.put("follows", account.getFollows());
        map.put("followers", account.getFollowedBy());

        FirestoreClient.getFirestore()
                .collection("users")
                .document(profile.getUid())
                .collection("history")
                .document().set(map);
    }
}





