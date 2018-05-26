package com.fett.mapper;

import com.fett.model.Search;
import me.postaddict.instagram.scraper.mapper.ModelMapper;

import java.io.InputStream;

public class ModelMapper2 extends ModelMapper implements Mapper2 {

    @Override
    public Search mapSearch(InputStream jsonStream) {
        Search search = mapObject(jsonStream, "com/fett/mapper/saerch.json", Search.class);
        return search;
    }
}