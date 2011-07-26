package com.gmail.rretzbach.seriesguy.services;

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

import javax.swing.JFileChooser;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

import com.gmail.rretzbach.seriesguy.model.SearchEngine;
import com.gmail.rretzbach.seriesguy.model.Series;
import com.thoughtworks.xstream.annotations.XStreamAlias;

public class MarshallingDataService implements DataService {

    protected static final String DATAFILE_PREF_KEY = "series.datafile";
    protected static final String IMGDIR_PREF_KEY = "series.imgdir";

    protected static Logger LOG = LoggerFactory
            .getLogger(MarshallingDataService.class);

    protected Marshaller marshaller;
    protected Unmarshaller unmarshaller;

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    @XStreamAlias("seriesdataset")
    public class SeriesDataset {
        private List<Series> series;
        private List<SearchEngine> engines;

        public void setEngines(List<SearchEngine> engines) {
            this.engines = engines;
        }

        public List<SearchEngine> getEngines() {
            return engines;
        }

        public void setSeries(List<Series> series) {
            this.series = series;
        }

        public List<Series> getSeries() {
            return series;
        }
    }

    protected List<Series> cachedSeries;
    protected List<SearchEngine> cachedSearchEngines;

    @Override
    public List<Series> findAllSeries() throws IOException {
        if (cachedSeries == null) {
            reload();
        }

        return cachedSeries;
    }

    protected File getFilePath() throws IOException {
        Preferences userRoot = Preferences.userRoot();

        String filePath = userRoot.get(DATAFILE_PREF_KEY, null);
        if (filePath != null) {
            return new File(filePath);
        }

        File file = chooseSaveLocation();
        if (file != null) {
            return file;
        }

        LOG.trace("Opening datafile cancelled by user");
        throw new RuntimeException("Opening datafile cancelled by user");
    }

    public File getImgDirPath() throws IOException {
        Preferences userRoot = Preferences.userRoot();

        String filePath = userRoot.get(IMGDIR_PREF_KEY, null);
        if (filePath != null) {
            return new File(filePath);
        }

        File file = chooseImageLocation();
        if (file != null) {
            return file;
        }

        throw new RuntimeException(
                "Choosing images directory cancelled by user");
    }

    public File chooseSaveLocation() {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Please choose a file to save series data");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Preferences userRoot = Preferences.userRoot();
            userRoot.put(DATAFILE_PREF_KEY, fc.getSelectedFile().toString());
            return fc.getSelectedFile();
        }

        return null;
    }

    public File chooseImageLocation() {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Please choose a directory to save series images");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Preferences userRoot = Preferences.userRoot();
            userRoot.put(IMGDIR_PREF_KEY, fc.getSelectedFile().toString());
            return fc.getSelectedFile();
        }

        return null;
    }

    @Override
    public List<SearchEngine> findAllSearchEngines() throws IOException {
        if (cachedSearchEngines == null) {
            reload();
        }

        return cachedSearchEngines;
    }

    @Override
    public void save() throws IOException {
        SeriesDataset seriesDataset = new SeriesDataset();
        seriesDataset.setEngines(cachedSearchEngines);
        seriesDataset.setSeries(cachedSeries);
        marshaller.marshal(seriesDataset, new StreamResult(new BufferedWriter(
                new FileWriter(getFilePath()))));
    }

    @Override
    public void reload() throws IOException {
        SeriesDataset seriesDataset;
        try {

            seriesDataset = (SeriesDataset) unmarshaller
                    .unmarshal(new StreamSource(new BufferedReader(
                            new FileReader(getFilePath()))));
            cachedSeries = seriesDataset.getSeries();
            cachedSearchEngines = seriesDataset.getEngines();
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading from xml file", e);
            cachedSeries = new ArrayList<Series>();
            cachedSearchEngines = new ArrayList<SearchEngine>();
        }

    }

    @Override
    public SearchEngine createSearchEngine() throws IOException {
        findAllSearchEngines();
        SearchEngine searchEngine = new SearchEngine();
        cachedSearchEngines.add(searchEngine);
        return searchEngine;
    }

    @Override
    public void deleteSearchEngine(SearchEngine searchEngine)
            throws IOException {
        findAllSearchEngines();
        cachedSearchEngines.remove(searchEngine);
    }

    @Override
    public Series createSeries() throws IOException {
        findAllSeries();
        Series series = new Series();
        cachedSeries.add(series);
        return series;
    }

    @Override
    public void deleteSeries(Series series) throws IOException {
        findAllSeries();
        cachedSeries.remove(series);
    }

}
