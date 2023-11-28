package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;

class WebCrawler {
    int noOfThreads;
    volatile DocumentDownloadThread threads[];
    private String URLs[];
    private HashSet<String> visitedURLs;
    private Queue<String> urlsQueue;
    private Queue<String> urlsDocProcessingQueue;
    private HashMap<String, Document> urlDocMap;
    private int maxDepth;
    private FileWriter multithreadedCrawlerStateFileWriters[];
    private FileWriter singlethreadedCrawlerStateFileWriters[];
    private static final Path currentPath = Paths.get(System.getProperty("user.dir"));
    private static final Path pathToDataDir = Paths.get(currentPath.toAbsolutePath().getParent().toString());

    class DocumentDownloadThread extends Thread {
        private Queue<String> urlsDocProcessingQueue;
        private HashMap<String, Document> urlDocMap;

        public DocumentDownloadThread(String name, Queue<String> urlsDocProcessingQueue,
                HashMap<String, Document> urlDocMap) {
            super(name);
            this.urlsDocProcessingQueue = urlsDocProcessingQueue;
            this.urlDocMap = urlDocMap;
        }

        public void run() {
            String currentURL;
            while ((currentURL = this.urlsDocProcessingQueue.poll()) != null) {
                Document doc = request(currentURL);
                this.urlDocMap.put(currentURL, doc);
            }
        }
    }

    public WebCrawler(String URLs[], int noOfThreads, int maxDepth) {
        this.noOfThreads = noOfThreads;
        this.URLs = URLs;
        this.visitedURLs = new HashSet<String>();
        this.threads = new DocumentDownloadThread[this.noOfThreads];
        this.urlsQueue = new LinkedList<String>();
        this.urlDocMap = new HashMap<>();
        this.urlsDocProcessingQueue = new LinkedList<>();
        this.maxDepth = maxDepth;
        this.multithreadedCrawlerStateFileWriters = new FileWriter[this.URLs.length];
        this.singlethreadedCrawlerStateFileWriters = new FileWriter[this.URLs.length];
        setupFileOperators();
    }

    public void releaseResources() {
        // release resources
    }

    private void setupFileOperators() {
        for (int i = 0; i < this.URLs.length; i++) {
            try {
                this.multithreadedCrawlerStateFileWriters[i] = new FileWriter(
                        pathToDataDir + "/data/multithreadedCrawler/root_url_" + (i + 1) + ".txt");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
            try {
                this.singlethreadedCrawlerStateFileWriters[i] = new FileWriter(
                        pathToDataDir + "/data/singlethreadedCrawler/root_url_" + (i + 1) + ".txt");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
    }

    private void createThreads() {
        for (int i = 0; i < this.noOfThreads; i++) {
            threads[i] = new DocumentDownloadThread("thread_" + i, this.urlsDocProcessingQueue, this.urlDocMap);
        }
    }

    private void startThreads() {
        for (int i = 0; i < this.noOfThreads; i++) {
            threads[i].start();
        }
    }

    private void stopThreads() {
        for (int i = 0; i < this.noOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < this.noOfThreads; i++) {
            threads[i] = null;
        }
    }

    private void clearState() {
        this.urlsQueue.clear();
        this.urlsDocProcessingQueue.clear();
        this.urlDocMap.clear();
    }

    private void writeMultithreadedWebCrawlerState(String line, int idx) {
        try {
            this.multithreadedCrawlerStateFileWriters[idx].write(line);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void closeMultithreadedWebCrawlerStateWriter(int idx) {
        try {
            this.multithreadedCrawlerStateFileWriters[idx].close();
        } catch (IOException e) {
        }
    }

    private void closeSinglethreadedWebCrawlerStateWriter(int idx) {
        try {
            this.singlethreadedCrawlerStateFileWriters[idx].close();
        } catch (IOException e) {
        }
    }

    private void writeSinglethreadedCrawlerState(String line, int idx) {
        try {
            this.singlethreadedCrawlerStateFileWriters[idx].write(line);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private CrawlerStateData loadMultithreadedWebCrawlerState() {
        BufferedReader reader;
        CrawlerStateData crawlerStateData = new CrawlerStateData();
        for (int i = 0; i < this.URLs.length; i++) {
            try {
                reader = new BufferedReader(
                        new FileReader(pathToDataDir + "/data/multithreadedCrawler/root_url_" + (i + 1) + ".txt"));
                String line = reader.readLine();
                while (line != null) {
                    line = line.trim();
                    if (line.startsWith("level:")) {
                        String lineSplit[] = line.split(": ");
                        int depth = Integer.parseInt(lineSplit[1]);
                        crawlerStateData.setDepth(depth);
                    } else {
                        if (!line.isEmpty() && line.startsWith("http")) {
                            crawlerStateData.setRootURL(line);
                            crawlerStateData.setIdx(i);
                        }
                    }
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (crawlerStateData.getRootURL().isEmpty()) {
            crawlerStateData.setRootURL(this.URLs[0]);
            crawlerStateData.setIdx(0);
        }
        return crawlerStateData;
    }

    private void loadSinglethreadedWebCrawlerState() {

    }

    public void startMultithreadedWebCrawler() {
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::: Multithreaded Web Crawler ::::::::::::::::::::::::::::::::::");
        for (int i = 0; i < this.URLs.length; i++) {
            createThreads();
            multiThreadedCrawl(URLs[i], i);
            clearState();
            stopThreads();
        }
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
    }

    public void startSinglethreadedWebCrawler() {
        this.visitedURLs.clear();
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::: Singlethreaded Web Crawler ::::::::::::::::::::::::::::::::::");
        for (int i = 0; i < this.URLs.length; i++) {
            singleThreadedCrawl(URLs[i], i);
            clearState();
        }
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
    }

    private Document request(String url) {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();
            if (con.response().statusCode() == 200) {
                return doc;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private void singleThreadedCrawl(String rootURL, int idx) {
        int depth = 0;
        this.urlsQueue.add(rootURL);
        this.visitedURLs.add(rootURL);

        System.out.println("\n>> Web crawling starting at rootURL: " + rootURL);
        System.out.println("Crawling...");

        writeSinglethreadedCrawlerState(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n",
                idx);
        writeSinglethreadedCrawlerState("\nrootURL: " + rootURL + "\n", idx);

        long startTime = System.currentTimeMillis();

        while (!this.urlsQueue.isEmpty() && depth < this.maxDepth) {
            writeSinglethreadedCrawlerState("\nlevel: " + depth + "\n", idx);
            int currentLevelSize = this.urlsQueue.size();
            for (int i = 0; i < currentLevelSize; i++) {
                String currentURL = this.urlsQueue.poll();
                writeSinglethreadedCrawlerState("\t" + currentURL + "\n", idx);
                Document doc = request(currentURL);
                if (doc != null) {
                    writeSinglethreadedCrawlerState("\tTitle: " + doc.title() + "\n", idx);
                    for (Element link : doc.select("a[href]")) {
                        String nextURL = link.absUrl("href");
                        if (!this.visitedURLs.contains(nextURL)) {
                            this.urlsQueue.add(nextURL);
                            this.visitedURLs.add(nextURL);
                        }
                    }
                }
            }
            depth++;
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(">> Web crawling ended for rootURL: " + rootURL);
        System.out.println("\n>> Analytics <<\n");
        System.out.println("Total time: " + elapsedTime + " ms");
        writeSinglethreadedCrawlerState(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::",
                idx);
        closeSinglethreadedWebCrawlerStateWriter(idx);
    }

    private void multiThreadedCrawl(String rootURL, int idx) {
        int depth = 0;
        int childThreadDocProcessedCount = 0;
        int mainThreadDocProcessedCount = 0;
        int threadsRunBreakpoint = 1;

        System.out.println("\n>> Web crawling starting at rootURL: " + rootURL);
        System.out.println("Crawling...");
        writeMultithreadedWebCrawlerState(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n",
                idx);
        writeMultithreadedWebCrawlerState("\nrootURL: " + rootURL + "\n", idx);

        this.urlsQueue.add(rootURL);
        this.visitedURLs.add(rootURL);

        long startTime = System.currentTimeMillis();

        while (!this.urlsQueue.isEmpty() && depth < this.maxDepth) {
            writeMultithreadedWebCrawlerState("\nlevel: " + depth + "\n", idx);
            int currentLevelSize = this.urlsQueue.size();
            for (int i = 0; i < currentLevelSize; i++) {
                String currentURL = this.urlsQueue.poll();
                writeMultithreadedWebCrawlerState("\t" + currentURL + "\n", idx);
                Document doc;
                if (this.urlDocMap.containsKey(currentURL)) {
                    doc = this.urlDocMap.get(currentURL);
                    childThreadDocProcessedCount++;
                } else {
                    doc = request(currentURL);
                    mainThreadDocProcessedCount++;
                }
                if (doc != null) {
                    writeMultithreadedWebCrawlerState("\tTitle: " + doc.title() + "\n", idx);
                    for (Element link : doc.select("a[href]")) {
                        String nextURL = link.absUrl("href");
                        if (!this.visitedURLs.contains(nextURL)) {
                            this.urlsQueue.add(nextURL);
                            this.urlsDocProcessingQueue.add(nextURL);
                            if (threadsRunBreakpoint == this.noOfThreads) {
                                startThreads();
                            }
                            this.visitedURLs.add(nextURL);
                            threadsRunBreakpoint++;
                        }
                    }
                }
            }
            depth++;
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(">> Web crawling ended for rootURL: " + rootURL);
        System.out.println("\n>> Analytics <<\n");
        System.out.println(
                "Total time: " + elapsedTime + " ms\nmainThreadDocProcessedCount: " + mainThreadDocProcessedCount
                        + "\nchildThreadDocProcessedCount: " + childThreadDocProcessedCount + "\n");
        writeMultithreadedWebCrawlerState(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::",
                idx);
        closeMultithreadedWebCrawlerStateWriter(idx);
    }
}