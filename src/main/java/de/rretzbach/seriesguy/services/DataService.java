package de.rretzbach.seriesguy.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.rretzbach.seriesguy.model.SearchEngine;
import de.rretzbach.seriesguy.model.Series;

public interface DataService {
	List<Series> findAllSeries();

	List<SearchEngine> findAllSearchEngines();

	SearchEngine createSearchEngine();

	void deleteSearchEngine(SearchEngine searchEngine);

	Series createSeries();

	void deleteSeries(Series series);

	void save(File basedir) throws IOException;

	void load(File basedir) throws IOException;

}
