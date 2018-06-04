package service;

import com.fett.Response.StandardResponse;
import com.fett.Response.StatusResponse;
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
import me.postaddict.instagram.scraper.model.PageInfo;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import com.google.api.Service;

@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class InstagramService{

    private Firestore db;
    private Instagram instagram;
    private Account account;

    @Autowired
    public InstagramService() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .addInterceptor(new UserAgentInterceptor(UserAgents.WIN10_CHROME))
                .addInterceptor(new ErrorInterceptor())
                .cookieJar(new DefaultCookieJar(new CookieHashSet()))
                .build();

        this.db = FirestoreClient.getFirestore();
        this.instagram = new Instagram(httpClient);
    }

    public void basePage() throws IOException{
        instagram.basePage();
    }

    public void login(String username, String password) throws IOException{
        instagram.login(username,password);
        if(account==null)
            account = getAccountByUsername(username);
    }

    public Account getAccountById(long id) throws IOException {
        return instagram.getAccountById(id);
    }


    public String getFollows(long id, boolean hasNext, String cursor){
        try {
            return instagram.getFollows(id, new PageInfo(hasNext,cursor));
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\":\"NOK\"}";
        }
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
    public Profile userRegister(Profile user){
        ApiFuture<com.google.firestore.v1beta1.WriteResult> result = db.collection("profile").document(user.getUid()).set(user.toMap());
        return user;
    }

    public String serach(String query){
        String result = null;
        try {
            result = instagram.getSearch(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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

    public void addWhitelist(Account b){
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
        ApiFuture<WriteResult> result = db
                .collection("users")
                .document(String.valueOf(account.getId()))
                .collection("whitelist")
                .document(String.valueOf(b.getId()))
                .set(map);
    }

    public StandardResponse unfollow(){
        new Thread() {
            @Override
            public void run() {
                List<String> whitelist = new ArrayList<String>();
                try {
                    int i = 0;
                    List<Account> f = instagram.getFollows(Long.valueOf("3472751680"), 15).getNodes();
                    while (f.size() > 500) {
                        for (Account a : f) {
                            if (!whitelist.contains(a.getUsername())) {
                                i++;
                                instagram.unfollowAccount(a.getId());
                                System.out.println("userid:" + a.getId() +"    username:"+a.getUsername());
                                sleep(3000);
                            }
                            if(i==15) {
                                System.out.println("Paused for 15min");
                                sleep(1000 * 60 * 15);
                                i = 0;
                            }
                        }
                        f = instagram.getFollows(Long.valueOf("3472751680"), 15).getNodes();
                    }
                } catch (UnknownHostException e) {
                    try {
                        System.out.println("UnkonowHostException");
                        System.out.println("Paused for 1 min");
                        sleep(1000 * 60);
                        unfollow();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return new StandardResponse(StatusResponse.SUCCESS, "Unfollow thread started");
    }

    public List<Account> getWhitelist(){
        List<Account> list = new ArrayList<Account>();
        try{
            CollectionReference docRef = db
                    .collection("users")
                    .document(String.valueOf(account.getId()))
                    .collection("whitelist");
            ApiFuture<QuerySnapshot> future = docRef.get();
            QuerySnapshot document = future.get();
            for (DocumentSnapshot doc:document) {
                list.add(doc.toObject(Account.class));
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void removeWhitelist(String id) {
        ApiFuture<WriteResult> result = db
                .collection("users")
                .document(String.valueOf(account.getId()))
                .collection("whitelist")
                .document(id)
                .delete();
    }
}