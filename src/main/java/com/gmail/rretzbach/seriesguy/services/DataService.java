package com.gmail.rretzbach.seriesguy.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.gmail.rretzbach.seriesguy.model.SearchEngine;
import com.gmail.rretzbach.seriesguy.model.Series;

public interface DataService {
	List<Series> findAllSeries();

	List<SearchEngine> findAllSearchEngines();

	SearchEngine createSearchEngine();

	void deleteSearchEngine(SearchEngine searchEngine);

	Series createSeries();

	void deleteSeries(Series series);

	void save() throws IOException;

	void saveAs(File file) throws IOException;

	void reload() throws IOException;

	void load(File file) throws IOException;

	File getImageDir();

	String getSourceDescription();

}
