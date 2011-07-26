package com.gmail.rretzbach.seriesguy.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("searchengine")
public class SearchEngine {
    protected String urlTemplate = "http://www.domain.tld?q={query}";
    protected String name = "Unnamed";

    public SearchEngine() {
    }

    public SearchEngine(String name, String urlTemplate) {
        this.name = name;
        this.urlTemplate = urlTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((urlTemplate == null) ? 0 : urlTemplate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchEngine other = (SearchEngine) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (urlTemplate == null) {
            if (other.urlTemplate != null)
                return false;
        } else if (!urlTemplate.equals(other.urlTemplate))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
