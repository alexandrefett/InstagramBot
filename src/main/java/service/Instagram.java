package service;

import com.fett.mapper.Mapper2;
import com.fett.mapper.ModelMapper2;
import com.google.gson.Gson;
import me.postaddict.instagram.scraper.AuthenticatedInsta;
import me.postaddict.instagram.scraper.MediaUtil;
import me.postaddict.instagram.scraper.exception.InstagramAuthException;
import me.postaddict.instagram.scraper.model.*;
import me.postaddict.instagram.scraper.request.*;
import me.postaddict.instagram.scraper.request.parameters.LocationParameter;
import me.postaddict.instagram.scraper.request.parameters.MediaCode;
import me.postaddict.instagram.scraper.request.parameters.TagName;
import me.postaddict.instagram.scraper.request.parameters.UserParameter;
import okhttp3.*;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpCookie;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class Instagram implements AuthenticatedInsta {

    private static final PageInfo FIRST_PAGE = new PageInfo(true, "");
    protected OkHttpClient httpClient;
    protected Mapper2 mapper;
    protected DelayHandler delayHandler;
    private String rhxgis;
    private String hash_follows;
    private String hash_followers;
    private PercistenceCookieStore cookieStore;

    public Instagram(OkHttpClient httpClient) {
        this(httpClient, new ModelMapper2(), new DefaultDelayHandler());
    }

    public Instagram(OkHttpClient httpClient, ModelMapper2 modelMapper, DefaultDelayHandler defaultDelayHandler) {
        this.httpClient = httpClient;
        this.mapper = modelMapper;
        this.delayHandler = defaultDelayHandler;
    }


    private void getHashFollows(String body) throws IOException{
        final String hash_follow = "Consumer.js/";
        String url = getJSFile(body, hash_follow);
        if(url!=null) {
            String jsbody = getJSBody(url);
            Pattern p1 = Pattern.compile("s=\"([a-f0-9]{32})\",l=1");
            Matcher m1 = p1.matcher(jsbody);
            if (m1.find()) {
                this.hash_follows =  m1.group(1);  // The matched substring
            }

            Pattern p2 = Pattern.compile("u=\"([a-f0-9]{32})\",");
            Matcher m2 = p2.matcher(jsbody);
            if (m2.find()) {
                this.hash_followers =  m2.group(1);  // The matched substring
            }
        }
    }

    private String getJSBody(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = executeHttpRequest(request);
        return response.body().string();
    }

    private String getJSFile(String body, String jsfile) throws IOException{
        String file = null;
        Pattern p1 = Pattern.compile(jsfile+"([a-f0-9]{12})");
        Matcher m1 = p1.matcher(body);
        if (m1.find()) {
            this.rhxgis =m1.group(1);  // The matched substring
            file = Endpoint.BASE_URL+Endpoint.BASE_STATIC+jsfile+m1.group(1)+".js";
        }
        return file;
    }

    protected Request withCsrfToken(Request request) {
        List<Cookie> cookies = httpClient.cookieJar()
                .loadForRequest(request.url());
        cookies.removeIf(cookie -> !cookie.name().equals("csrftoken"));
        if (!cookies.isEmpty()) {
            Cookie cookie = cookies.get(0);
            return request.newBuilder()
                    .addHeader("X-CSRFToken", cookie.value())
                    .build();
        }
        return request;
    }

    public void basePage() throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.BASE_URL)
                .build();

        Response response = executeHttpRequest(request);
        String body = response.body().string();
        getRhxGis(body);
//        try (ResponseBody body = response.body()){
        //release connection
//        }
    }
    public boolean basePageHash(String _token) throws IOException {
        boolean logged = false;
        this.cookieStore  = new PercistenceCookieStore(_token);
        Request request = new Request.Builder()
                .url(Endpoint.BASE_URL)
                .build();

        if(cookieStore.getCookies().size()>0) {
            List<HttpCookie> cookielist = cookieStore.getCookies();
            List<Cookie> cookie3 = new ArrayList<Cookie>();
            for (HttpCookie cookie:cookielist) {
                Cookie c = new Cookie.Builder()
                        .domain(cookie.getDomain())
                        .value(cookie.getValue())
                        .expiresAt(cookie.getMaxAge())
                        .hostOnlyDomain(cookie.getDomain())
                        .name(cookie.getName())
                        .path(cookie.getPath())
                        .build();
                cookie3.add(c);
            }
            logged = true;
            httpClient.cookieJar().saveFromResponse(request.url(), cookie3);
        }
        Response response = executeHttpRequest(request);
        String body = response.body().string();
        getHashFollows(body);
        getRhxGis(body);
        return logged;
//        try (ResponseBody body = response.body()){
        //release connection
//        }
    }

    private void getRhxGis(String body) throws IOException {
        Pattern p1 = Pattern.compile("\"rhx_gis\":\"([a-f0-9]{32})\"");
        Matcher m1 = p1.matcher(body);
        if (m1.find()) {
            this.rhxgis =m1.group(1);  // The matched substring
        }
    }

    public String login(String username, String password, String extra) throws IOException {

            if (username == null || password == null) {
                throw new InstagramAuthException("Specify username and password");
            }
            //cookieStore.
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url(Endpoint.LOGIN_URL)
                    .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                    .post(formBody)
                    .build();

            Response response = executeHttpRequest(withCsrfToken(request));


        String body = response.body().string();
            return body;
        //try(InputStream jsonStream = response.body().byteStream()) {
        //   if(!mapper.isAuthenticated(jsonStream)){
        //      throw new InstagramAuthException("Credentials rejected by instagram");
        // }
        //}
    }

    public void login(String username, String password) throws IOException {
        if (username == null || password == null) {
            throw new InstagramAuthException("Specify username and password");
        }

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(Endpoint.LOGIN_URL)
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .post(formBody)
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        String body = response.body().string();
        //try(InputStream jsonStream = response.body().byteStream()) {
        //   if(!mapper.isAuthenticated(jsonStream)){
        //      throw new InstagramAuthException("Credentials rejected by instagram");
        // }
        //}
    }

    public void solveChallenge(String challenge) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("choice", "0")
                .build();

        Request request = new Request.Builder()
                .url(Endpoint.BASE_URL+"/challenge/")
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .post(formBody)
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        String body = response.body().string();
        //try(InputStream jsonStream = response.body().byteStream()) {
        //   if(!mapper.isAuthenticated(jsonStream)){
        //      throw new InstagramAuthException("Credentials rejected by instagram");
        // }
        //}
    }

    public Account getAccountById(long id) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.getAccountJsonInfoLinkByAccountId(id))
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .build();
        Response response = executeHttpRequest(withCsrfToken(request));
        try(InputStream jsonStream = response.body().byteStream()) {
            return getMediaByCode(mapper.getLastMediaShortCode(jsonStream)).getOwner();
        }
    }

    private String genMD5(String rhxgis, String variables){
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String v = String.join(":", rhxgis, "/"+variables+"/");
        m.update(v.getBytes(),0,v.length());
        String md5 = new BigInteger(1,m.digest()).toString(16);
        return md5;
    }

    public Account getAccountByUsername(String username) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.getAccountId(username))
                .addHeader(Endpoint.REFERER, Endpoint.BASE_URL + "/"+username+ "/")
                .addHeader(Endpoint.X_INSTAGRAM_GIS, genMD5(this.rhxgis, username))
                .addHeader("X-Requested-with", "XMLHttpRequest")
                .build();
        Response response = executeHttpRequest(request);

        List<HttpCookie> cookielist = new ArrayList<HttpCookie>();
        List<Cookie> cookie3 = httpClient.cookieJar().loadForRequest(request.url());
        for (Cookie cookie:cookie3) {
            HttpCookie c = new HttpCookie(cookie.name(),cookie.value());
            c.setDomain(cookie.domain());
            c.setHttpOnly(cookie.httpOnly());
            c.setMaxAge(cookie.expiresAt());
            c.setPath(cookie.path());
            c.setSecure(cookie.secure());
            cookielist.add(c);
            cookieStore.add(URI.create(Endpoint.BASE_URL), c);
        }
        cookieStore.save();

        try(InputStream jsonStream = response.body().byteStream()) {
            return mapper.mapAccount(jsonStream);
        }
    }

    public String getSearch(String username) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.getSearchUserByUsername(username))
                .addHeader(Endpoint.REFERER, Endpoint.BASE_URL)
                .build();

        Response response = executeHttpRequest(request);
        String jsonStream = response.body().string();
        Gson gson = new Gson();

        return jsonStream;
    }

    public PageObject<Media> getMedias(String username, int pageCount) throws IOException {
        long userId = getAccountByUsername(username).getId();
        return getMedias(userId, pageCount, FIRST_PAGE);
    }

    public PageObject<Media> getMedias(long userId, int pageCount, PageInfo pageCursor) throws IOException {
        GetMediasRequest getMediasRequest = new GetMediasRequest(httpClient, mapper, delayHandler);
        return getMediasRequest.requestInstagramResult(new UserParameter(userId), pageCount, pageCursor);
    }

    public Media getMediaByUrl(String url) throws IOException {
        String urlRegexp = Endpoint.getMediaPageLinkByCodeMatcher();
        if(url==null || !url.matches(urlRegexp)){
            throw new IllegalArgumentException("Media URL not matches regexp: "+urlRegexp+" current value: "+url);
        }
        Request request = new Request.Builder()
                .url(url + "/?__a=1")
                .build();

        Response response = executeHttpRequest(request);
        try(InputStream jsonStream = response.body().byteStream()) {
            return mapper.mapMedia(jsonStream);
        }
    }

    public Media getMediaByCode(String code) throws IOException {
        return getMediaByUrl(Endpoint.getMediaPageLinkByCode(code));
    }

    public Tag getTagByName(String tagName) throws IOException {
        validateTagName(tagName);
        Request request = new Request.Builder()
                .url(Endpoint.getTagJsonByTagName(tagName))
                .build();

        Response response = executeHttpRequest(request);
        try(InputStream jsonStream = response.body().byteStream()) {
            return mapper.mapTag(jsonStream);
        }
    }

    public Location getLocationMediasById(String locationId, int pageCount) throws IOException {
        GetLocationRequest getLocationRequest = new GetLocationRequest(httpClient, mapper, delayHandler);
        return getLocationRequest.requestInstagramResult(new LocationParameter(locationId), pageCount, FIRST_PAGE);
    }

    public Tag getMediasByTag(String tag, int pageCount) throws IOException {
        validateTagName(tag);
        GetMediaByTagRequest getMediaByTagRequest = new GetMediaByTagRequest(httpClient, mapper, delayHandler);
        return getMediaByTagRequest.requestInstagramResult(new TagName(tag), pageCount, FIRST_PAGE);
    }

    public PageObject<Comment> getCommentsByMediaCode(String code, int pageCount) throws IOException {
        GetCommentsByMediaCode getCommentsByMediaCode = new GetCommentsByMediaCode(httpClient, mapper, delayHandler);
        return getCommentsByMediaCode.requestInstagramResult(new MediaCode(code), pageCount,
                new PageInfo(true,"0"));
    }

    public void likeMediaByCode(String code) throws IOException {
        String url = Endpoint.getMediaLikeLink(MediaUtil.getIdFromCode(code));
        Request request = new Request.Builder()
                .url(url)
                .header(Endpoint.REFERER, Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(new FormBody.Builder().build())
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        response.body().close();
    }

    public void followAccountByUsername(String username) throws IOException{
        Account account = getAccountByUsername(username);
        followAccount(account.getId());
    }

    public void followAccount(long userId) throws IOException {
        String url = Endpoint.getFollowAccountLink(userId);
        Request request = new Request.Builder()
                .url(url)
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .post(new FormBody.Builder().build())
                .build();
        Response response = executeHttpRequest(withCsrfToken(request));
        response.body().close();
    }

    public void unfollowAccountByUsername(String username) throws IOException{
        Account account = getAccountByUsername(username);
        unfollowAccount(account.getId());
    }

    public void unfollowAccount(long userId) throws IOException {
        String url = Endpoint.getUnfollowAccountLink(userId);
        Request request = new Request.Builder()
                .url(url)
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .post(new FormBody.Builder().build())
                .build();
        Response response = executeHttpRequest(withCsrfToken(request));
        response.body().close();
    }

    public PageObject<Account> getMediaLikes(String shortcode, int pageCount) throws IOException{
        GetMediaLikesRequest getMediaLikesRequest = new GetMediaLikesRequest(httpClient, mapper, delayHandler);
        return getMediaLikesRequest.requestInstagramResult(new MediaCode(shortcode), pageCount, FIRST_PAGE);
    }

    public PageObject<Account> getFollows(long userId, int pageCount) throws IOException {
        GetFollowsRequest getFollowsRequest = new GetFollowsRequest(httpClient, mapper, delayHandler);
        return getFollowsRequest.requestInstagramResult(new UserParameter(userId), pageCount, FIRST_PAGE);
    }

    public String getFollows(long userId, PageInfo pageInfo) throws IOException {

        Request request = new Request.Builder()
                .url(Endpoint.getFollowsLinkVariables(hash_follows, userId, 50, pageInfo.getEndCursor()))
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .build();

        Response response = executeHttpRequest(request);
        String jsonStream = response.body().string();

        return jsonStream;
    }

    public String getFollowers(long userId, PageInfo pageInfo) throws IOException {

        Request request = new Request.Builder()
                .url(Endpoint.getFollowersLinkVariables(hash_followers, userId, 50, pageInfo.getEndCursor()))
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .build();

        Response response = executeHttpRequest(request);
        String jsonStream = response.body().string();

        return jsonStream;
    }

    public PageObject<Account> getFollowers(long userId, int pageCount) throws IOException {
        GetFollowersRequest getFollowersRequest = new GetFollowersRequest(httpClient, mapper, delayHandler);
        return getFollowersRequest.requestInstagramResult(new UserParameter(userId),pageCount, FIRST_PAGE);
    }

    public void unlikeMediaByCode(String code) throws IOException {
        String url = Endpoint.getMediaUnlikeLink(MediaUtil.getIdFromCode(code));
        Request request = new Request.Builder()
                .url(url)
                .header(Endpoint.REFERER, Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(new FormBody.Builder().build())
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        response.body().close();
    }

    public ActionResponse<Comment> addMediaComment(String code, String commentText) throws IOException {
        String url = Endpoint.addMediaCommentLink(MediaUtil.getIdFromCode(code));
        FormBody formBody = new FormBody.Builder()
                .add("comment_text", commentText)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .header(Endpoint.REFERER, Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(formBody)
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        try(InputStream jsonStream = response.body().byteStream()) {
            return mapper.mapMediaCommentResponse(jsonStream);
        }
    }

    public void deleteMediaComment(String code, String commentId) throws IOException {
        String url = Endpoint.deleteMediaCommentLink(MediaUtil.getIdFromCode(code), commentId);
        Request request = new Request.Builder()
                .url(url)
                .header(Endpoint.REFERER, Endpoint.getMediaPageLinkByCode(code) + "/")
                .post(new FormBody.Builder().build())
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        response.body().close();
    }

    @Override
    public ActivityFeed getActivityFeed() throws IOException{

        Request request = new Request.Builder()
                .url(Endpoint.ACTIVITY_FEED)
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        try(InputStream jsonStream = response.body().byteStream()) {
            ActivityFeed activityFeed = mapper.mapActivity(jsonStream);
            markActivityChecked(activityFeed);
            return activityFeed;
        }
    }

    private void markActivityChecked(ActivityFeed activityFeed) throws IOException {
        Request request = new Request.Builder()
                .url(Endpoint.ACTIVITY_MARK_CHECKED)
                .header(Endpoint.REFERER, Endpoint.BASE_URL + "/")
                .post(new FormBody.Builder().add("timestamp", activityFeed.getTimestamp()).build())
                .build();
        try (ResponseBody response = executeHttpRequest(withCsrfToken(request)).body()){
            //skip
        }
    }

    protected Response executeHttpRequest(Request request) throws IOException {
        Response response = this.httpClient.newCall(request).execute();
        if(delayHandler!=null){
            delayHandler.onEachRequest();
        }
        return response;
    }

    private void validateTagName(String tag) {
        if(tag==null || tag.isEmpty() || tag.startsWith("#")){
            throw new IllegalArgumentException("Please provide non empty tag name that not starts with #");
        }
    }
}

