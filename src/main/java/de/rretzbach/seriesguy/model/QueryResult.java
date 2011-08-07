package de.rretzbach.seriesguy.model;

public class QueryResult {
    private Series series;
    private int offset;
    private String line;

    public QueryResult(Series series, int offset, String line) {
        this.series = series;
        this.offset = offset;
        this.line = line;
    }

    public Series getSeries() {
        return series;
    }

    public int getOffset() {
        return offset;
    }

    public String getLine() {
        return line;
    }

    @Override
    public String toString() {
        return String.format(
                "%-25s [%6s] - %s",
                series.getName(),
                series.getFormattedEpisode(series.getSeason(),
                        series.getLastSeenEpisode() + offset), line);
    }
}
