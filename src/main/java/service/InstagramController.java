package service;

import com.fett.Response.StandardResponse;
import com.fett.model.User;
import me.postaddict.instagram.scraper.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class InstagramController {

    private static final String API_CONTEXT = "/api/v1";

    @Autowired
    private InstagramService instagram;


    @RequestMapping("/login")
    public String _login(@RequestParam(value="username") String username, @RequestParam(value="password") String password) {
        try {
            instagram.basePage();
            instagram.login(username, password);
            instagram.basePage();
        } catch (IOException e) {
            e.printStackTrace();
            return "NOT OK";
        }
        return "OK";
    }

    @RequestMapping("/account")
    public Account _getAccount(@RequestParam(value="username") String username) {
        Account account = null;
        try {
            account = instagram.getAccountByUsername(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    @RequestMapping("/register")
    public User _postRegister(@RequestParam(value="user") User user) {
        return instagram.userRegister(user);
    }

    @RequestMapping("/follow")
    public StandardResponse _doFollow(@RequestParam(value="username") String username) {
        return instagram.follow(username);
    }
}

