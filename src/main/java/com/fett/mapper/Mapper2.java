package com.fett.mapper;

import com.fett.model.Search;
import me.postaddict.instagram.scraper.mapper.Mapper;

import java.io.InputStream;

public interface Mapper2 extends Mapper {
    Search mapSearch(InputStream jsonStream);
}