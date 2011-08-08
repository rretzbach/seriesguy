package de.rretzbach.seriesguy.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.rretzbach.seriesguy.model.SearchEngine;
import de.rretzbach.seriesguy.model.Series;

@Component
public class MarshallingDataService implements DataService {

	protected static Logger LOG = LoggerFactory
			.getLogger(MarshallingDataService.class);

	protected XStreamMarshaller marshaller;

	@XStreamAlias("seriesdataset")
	public static class SeriesDataset {
		private List<Series> series;
		private List<SearchEngine> engines;

		public void setEngines(List<SearchEngine> engines) {
			this.engines = engines;
		}

		public List<SearchEngine> getSearchEngines() {
			if (engines == null) {
				engines = new ArrayList<SearchEngine>();
			}
			return engines;
		}

		public void setSeries(List<Series> series) {
			this.series = series;
		}

		public List<Series> getSeries() {
			if (series == null) {
				series = new ArrayList<Series>();
			}
			return series;
		}
	}

	protected SeriesDataset dataset;

	public MarshallingDataService() {

	}

	@PostConstruct
	public void init() {
		marshaller = new XStreamMarshaller();
		marshaller.setAnnotatedClass(SeriesDataset.class);
		marshaller.setAnnotatedClass(Series.class);
		marshaller.setAnnotatedClass(SearchEngine.class);

		dataset = new SeriesDataset();
	}

	@Override
	public List<Series> findAllSeries() {
		return dataset.getSeries();
	}

	@Override
	public List<SearchEngine> findAllSearchEngines() {
		return dataset.getSearchEngines();
	}

	@Override
	public void save(File basedir) throws IOException {
		marshaller.marshal(dataset, new StreamResult(new BufferedWriter(
				new FileWriter(new File(basedir, "series.xml")))));
	}

	@Override
	public SearchEngine createSearchEngine() {
		SearchEngine searchEngine = new SearchEngine();
		dataset.getSearchEngines().add(searchEngine);
		return searchEngine;
	}

	@Override
	public void deleteSearchEngine(SearchEngine searchEngine) {
		dataset.getSearchEngines().remove(searchEngine);
	}

	@Override
	public Series createSeries() {
		findAllSeries();
		Series series = new Series();
		dataset.getSeries().add(series);
		return series;
	}

	@Override
	public void deleteSeries(Series series) {
		findAllSeries();
		dataset.getSeries().remove(series);
	}

	@Override
	public void load(File basedir) throws IOException {
		File file = new File(basedir, "series.xml");
		if (file.exists()) {
			dataset = (SeriesDataset) marshaller.unmarshal(new StreamSource(
					new BufferedReader(new FileReader(file))));
		}
	}

}
