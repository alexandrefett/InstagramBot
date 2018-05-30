package service;

import com.fett.Response.StandardResponse;
import com.fett.Response.StatusResponse;
import com.fett.model.User;
import me.postaddict.instagram.scraper.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
public class InstagramController {

    private static final String API_CONTEXT = "/api/v1";

    @Autowired
    private InstagramService instagram;


    @RequestMapping(value = API_CONTEXT+"/login", method = RequestMethod.POST)
    public StandardResponse _login(@RequestParam(value="username") String username, @RequestParam(value="password") String password) {
        try {
            instagram.basePage();
            instagram.login(username, password);
            instagram.basePage();
        } catch (IOException e) {
            e.printStackTrace();
            return new StandardResponse(StatusResponse.ERROR, e.getMessage());
        }
        return new StandardResponse(StatusResponse.SUCCESS, "ok");
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

    @RequestMapping(value = API_CONTEXT+"/register", method = RequestMethod.POST)
    public ResponseEntity<User> _postRegister(@RequestParam(value="user") User user) {
        if (user != null) {
            instagram.userRegister(user);
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
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
    public List<User> _getWhitelist() {
        return instagram.getWhitelist();
    }

    @RequestMapping(API_CONTEXT+"/whitelist/remove")
    public String _removeWhitelist(@RequestParam(value="id") String id) {
        instagram.removeWhitelist(id);
        return StatusResponse.SUCCESS.toString();

    }

}

