package de.rretzbach.seriesguy.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Component;

import de.rretzbach.seriesguy.model.SearchEngine;
import de.rretzbach.seriesguy.model.Series;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@Component
public class MarshallingDataService implements DataService {

	protected static final String DATAFILE_PREF_KEY = "series.datafile";

	protected static Logger LOG = LoggerFactory
			.getLogger(MarshallingDataService.class);

	protected XStreamMarshaller marshaller;

	protected File datafile;
	protected File imagedir;

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

		Preferences userRoot = Preferences.userRoot();
		{
			String filepath = userRoot.get(DATAFILE_PREF_KEY, null);
			if (filepath != null) {
				datafile = new File(filepath);
				try {
					reload();
				} catch (Exception e) {
					LOG.error("Error while loading data", e);
				}
			}
		}
	}

	@Override
	public List<Series> findAllSeries() {
		return dataset.getSeries();
	}

	public File getImageDir() {
		return getImageDir(datafile);
	}

	protected File getImageDir(File datafile) {
		return new File(datafile, "seriesguy_images");
	}

	@Override
	public List<SearchEngine> findAllSearchEngines() {
		return dataset.getSearchEngines();
	}

	@Override
	public void save() throws IOException {
		marshaller.marshal(dataset, new StreamResult(new BufferedWriter(
				new FileWriter(datafile))));
	}

	@Override
	public void reload() throws IOException {
		try {
			dataset = (SeriesDataset) marshaller.unmarshal(new StreamSource(
					new BufferedReader(new FileReader(datafile))));
		} catch (FileNotFoundException e) {
			LOG.error("Error while loading from xml file", e);
		}

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
	public void saveAs(File selectedFile) throws IOException {
		switchDatafile(selectedFile);
		save();
	}

	public boolean switchDatafile(File datafile) {
		boolean needsSwitch = this.datafile.equals(datafile);
		if (needsSwitch) {
			LOG.debug("datafile is being switched");
			copyImages(datafile);
			this.datafile = datafile;
			savePreferences();
		}
		return needsSwitch;
	}

	private void copyImages(File datafile) {
		File srcDir = getImageDir(datafile);
		File destDir = getImageDir(datafile);
		try {
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e) {
			LOG.error("Error while copying images", e);
		}
	}

	public void savePreferences() {
		Preferences userRoot = Preferences.userRoot();
		userRoot.put(DATAFILE_PREF_KEY, this.datafile.getAbsolutePath());
	}

	@Override
	public void load(File file) throws IOException {
		switchDatafile(file);
		reload();
	}

	@Override
	public String getSourceDescription() {
		return datafile.getAbsolutePath();
	}

}
