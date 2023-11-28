package main;

public class CrawlerStateData {
    private String rootURL;
    private int idx;
    private int depth;

    public CrawlerStateData() {
        this.rootURL = "";
        this.idx = 0;
        this.depth = 0;
    }

    public void setRootURL(String rootURL) {
        this.rootURL = rootURL;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getRootURL() {
        return this.rootURL;
    }

    public int getIdx() {
        return this.idx;
    }

    public int getDepth() {
        return this.depth;
    }

    @Override
    public String toString() {
        return "rootURL: " + this.rootURL + " depth: " + this.depth + " idx: " + this.idx;
    }
}