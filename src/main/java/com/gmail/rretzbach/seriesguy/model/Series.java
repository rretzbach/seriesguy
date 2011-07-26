package com.gmail.rretzbach.seriesguy.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("series")
public class Series {
    protected String name = "Unnamed";

    protected Integer lastSeenEpisode = 1;

    protected String regex = "{name} {episode}";

    protected Date lastModified = new Date();

    protected Boolean finished = false;

    protected SearchEngine searchEngine = null;

    protected Integer season = 1;

    protected String episodeFormat = "s{{season},number,00}e{{episode},number,00}";

    protected String searchText = "{episode} {name}";

    protected String imagePath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setLastModified(new Date());
    }

    public Integer getLastSeenEpisode() {
        return lastSeenEpisode;
    }

    public void setLastSeenEpisode(Integer lastSeenEpisode) {
        this.lastSeenEpisode = lastSeenEpisode;
        setLastModified(new Date());
    }

    public String getSearchText() {
        if (searchText == null)
            return "";
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        setLastModified(new Date());
    }

    public Integer getSeason() {
        if (season == null)
            return 1;
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String getEpisodeFormat() {
        if (episodeFormat == null)
            return "s{{season},number,00}e{{episode},number,00}";
        return episodeFormat;
    }

    public void setEpisodeFormat(String episodeFormat) {
        this.episodeFormat = episodeFormat;
    }

    protected String getFormattedEpisode(Integer season, Integer episode) {
        // TODO remove default
        if (episodeFormat == null) {
            episodeFormat = "{{episode}}";
        }
        String localFormat = episodeFormat.replace("{season}", "0").replace(
                "{episode}", "1");
        return MessageFormat.format(localFormat, season, episode);
    }

    public CharSequence getFormattedEpisode(Integer episode) {
        return getFormattedEpisode(season, episode);
    }

    public String getFormattedEpisode() {
        return getFormattedEpisode(season, lastSeenEpisode);
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getRegex() {
        if (regex == null)
            return "";
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
        setLastModified(new Date());
    }

    public String getRegex(Integer offset) {
        return regex.replace("{episode}",
                getFormattedEpisode(lastSeenEpisode + offset)).replace(
                "{name}", getName());
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public void setSearchEngine(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void seenCurrentEpisode() {
        lastSeenEpisode += 1;
    }

    public String getSearchEngineURL(int offset)
            throws UnsupportedEncodingException {
        String query = searchText.replace("{episode}",
                getFormattedEpisode(lastSeenEpisode + offset)).replace(
                "{name}", getName());
        query = URLEncoder.encode(query, "UTF-8");
        String urlTemplate = searchEngine.getUrlTemplate();
        return urlTemplate.replace("{query}", query);
    }

    @Override
    public String toString() {
        return name + " " + getFormattedEpisode(season, lastSeenEpisode);
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }
}
