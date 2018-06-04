package service;

import com.fett.Response.StandardResponse;
import com.fett.Response.StatusResponse;
import com.fett.model.Profile;
import com.fett.model.Search;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.JsonElement;
import me.postaddict.instagram.scraper.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
public class InstagramController {

    private static final String API_CONTEXT = "/api/v1";

    @Autowired
    private InstagramService instagram;

    @PostConstruct
    public void init(){
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("instamanager-908a3-aab9f9f25fd5.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://instamanager-908a3.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(value = API_CONTEXT+"/login", method = RequestMethod.POST)
    public ResponseEntity<Account> _login(@RequestBody Profile user) {
        Account account = null;
        try {
            instagram.basePage();
            instagram.login(user.getUsername(), user.getPassword());
            instagram.basePage();
            account = instagram.getAccountByUsername(user.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Account>(account, HttpStatus.OK);
    }

    @RequestMapping(API_CONTEXT+"/account")
    public Account _getAccount(@RequestParam(value="username") String username) {
        Account account = null;
        try {
            account = instagram.getAccountByUsername(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    @RequestMapping(API_CONTEXT+"/search")
    public String _getsearch(@RequestParam(value="query") String query) {
        return instagram.serach(query);
    }


    @RequestMapping(value = API_CONTEXT+"/register", method = RequestMethod.POST)
    public ResponseEntity<Profile> _postRegister(@RequestBody Profile user) {
        if (user != null) {
            instagram.userRegister(user);
        }
        return new ResponseEntity<Profile>(user, HttpStatus.OK);
    }

    @RequestMapping(API_CONTEXT+"/follow")
    public StandardResponse _doFollow(@RequestParam(value="username") String username) {
        return instagram.follow(username);
    }

    @RequestMapping(API_CONTEXT+"/unfollow")
    public StandardResponse _doUnfollow() {
        return instagram.unfollow();
    }

    @RequestMapping(value = API_CONTEXT+"/whitelist/add", method = RequestMethod.POST)
    public ResponseEntity<String> _addWhitelist(@RequestBody Account account) {
        if(account !=null){
            instagram.addWhitelist(account);
        }
        return new ResponseEntity<String>(StatusResponse.SUCCESS.toString(), HttpStatus.OK);
    }

    @RequestMapping(API_CONTEXT+"/whitelist/list")
    public List<Account> _getWhitelist() {
        return instagram.getWhitelist();
    }

    @RequestMapping(API_CONTEXT+"/whitelist/remove")
    public String _removeWhitelist(@RequestParam(value="id") String id) {
        instagram.removeWhitelist(id);
        return StatusResponse.SUCCESS.toString();

    }

}

