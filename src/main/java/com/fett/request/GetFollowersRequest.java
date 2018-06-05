package com.fett.request;

import me.postaddict.instagram.scraper.mapper.Mapper;
import me.postaddict.instagram.scraper.model.Account;
import me.postaddict.instagram.scraper.model.PageInfo;
import me.postaddict.instagram.scraper.model.PageObject;
import me.postaddict.instagram.scraper.request.DelayHandler;
import me.postaddict.instagram.scraper.request.PaginatedRequest;
import me.postaddict.instagram.scraper.request.parameters.UserParameter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import service.Endpoint;

import java.io.InputStream;

public class GetFollowersRequest extends PaginatedRequest<PageObject<Account>, UserParameter> {
    public GetFollowersRequest(OkHttpClient httpClient, Mapper mapper, DelayHandler delayHandler) {
        super(httpClient, mapper, delayHandler);
    }

    protected Request requestInstagram(UserParameter requestParameters, PageInfo pageInfo) {
        return (new Builder()).url(Endpoint.getFollowersLinkVariables("", requestParameters.getUserId(), 50, pageInfo.getEndCursor())).header("Referer", "https://www.instagram.com/").build();
    }

    protected void updateResult(PageObject<Account> result, PageObject<Account> current) {
        result.getNodes().addAll(current.getNodes());
        result.setPageInfo(current.getPageInfo());
    }

    protected PageInfo getPageInfo(PageObject<Account> current) {
        return current.getPageInfo();
    }

    protected PageObject<Account> mapResponse(InputStream jsonStream) {
        return this.getMapper().mapFollowers(jsonStream);
    }
}
