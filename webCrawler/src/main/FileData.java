package main;

public class FileData {
    private int expectedURLsCount;
    private int actualURLsCount;
    private String url;
    private int level;

    public FileData(int expectedURLsCount, int actualURLsCount, String url, int level) {
        this.expectedURLsCount = expectedURLsCount;
        this.actualURLsCount = actualURLsCount;
        this.url = url;
        this.level = level;
    }

    public void setExpectedURLsCount(int expectedURLsCount) {
        this.expectedURLsCount = expectedURLsCount;
    }

    public void setActualURLsCount(int actualURLsCount) {
        this.actualURLsCount = actualURLsCount;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public int getExpectedURLsCount() {
        return this.expectedURLsCount;
    }

    public int getActualURLsCount() {
        return this.actualURLsCount;
    }

    public String getURL() {
        return this.url;
    }

    public String toString() {
        return "expectedURLsCount: " + this.expectedURLsCount + " actualURLsCount: " + this.actualURLsCount
                + " url: " + this.url + " Level: " + this.level;
    }

    public int getLevel() {
        return this.level;
    }
}