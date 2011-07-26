package com.gmail.rretzbach.seriesguy.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.gmail.rretzbach.seriesguy.model.SearchEngine;
import com.gmail.rretzbach.seriesguy.model.Series;

public interface DataService {
    List<Series> findAllSeries() throws IOException;

    List<SearchEngine> findAllSearchEngines() throws IOException;

    SearchEngine createSearchEngine() throws IOException;

    void deleteSearchEngine(SearchEngine searchEngine) throws IOException;

    Series createSeries() throws IOException;

    void deleteSeries(Series series) throws IOException;

    void save() throws IOException;

    void reload() throws IOException;

    File chooseSaveLocation();

    File chooseImageLocation();

    File getImgDirPath() throws IOException;

}
