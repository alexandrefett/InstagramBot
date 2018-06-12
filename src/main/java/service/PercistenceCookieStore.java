package service;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PercistenceCookieStore  implements CookieStore, Runnable {

    CookieStore store;
    Gson gson;
    String outputFile;
    Map<String, String> cookies;
    String name;

    public PercistenceCookieStore(String name) {

        store = new CookieManager().getCookieStore();
        gson = new Gson();
        this.name = name;
        outputFile = name+"cookies.json";

        try {
            Type typeOfHashMap = new TypeToken<Map<String, String>>() { }.getType();
            Reader reader = new FileReader(outputFile);

            cookies = gson.fromJson(reader, typeOfHashMap);
            // Load cookies from file
            for (Map.Entry<String, String> entry: cookies.entrySet()) {
                HttpCookie cookie = gson.fromJson(entry.getValue(), HttpCookie.class);
                store.add(URI.create(cookie.getDomain()), cookie);
            }
        } catch (FileNotFoundException | JsonSyntaxException | JsonIOException | NullPointerException e) {
            System.err.println("Oops. File doesn't exist");
            cookies = new HashMap<>();
        }

        // Deserialize cookies into file on vm shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        System.out.println("Adding: "+gson.toJson(cookie));
        cookies.put(cookie.getDomain() + "|" + cookie.getName(), gson.toJson(cookie));
        store.add(URI.create(cookie.getDomain()), cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return store.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return store.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return store.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        cookies.remove(cookie.getDomain() + "|" + cookie.getName());
        return store.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        cookies.clear();
        return store.removeAll();
    }

    @Override
    public void run() {
        try {
            Writer writer = new FileWriter(outputFile);
            gson.toJson(cookies, writer);
            writer.flush();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    protected String getCookieToken(URI uri, HttpCookie cookie) {
        return cookie.getName() + cookie.getDomain();
    }

}