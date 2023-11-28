package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.io.FileWriter;
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
    private FileWriter multithreadedCrawlerStateFileWriter;
    private FileWriter singlethreadedCrawlerStateFileWriter;
    private static final Path currentPath = Paths.get("");
    private static final Path pathToDataDir = Paths.get(currentPath.toAbsolutePath().toString(),
            "multithreaded-web-crawler", "webCrawler", "data");

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
        try {
            this.multithreadedCrawlerStateFileWriter = new FileWriter(
                    pathToDataDir + "/multithreadedWebCrawlerState.txt");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        try {
            this.singlethreadedCrawlerStateFileWriter = new FileWriter(
                    pathToDataDir + "/singlethreadedCrawlerState.txt");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
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

    private void writeMultithreadedWebCrawlerState(ArrayList<String> logs) {
        System.out.println(
                "\n>> Multithreaded crawler state writing started for the file: multithreadedWebCrawlerState.txt");
        System.out.println("Writing ");
        for (String line : logs) {
            try {
                this.multithreadedCrawlerStateFileWriter.write(line);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
        System.out.println(
                ">> Multithreaded crawler state writing ended for the file:  multithreadedWebCrawlerState.txt");
    }

    private void writeSinglethreadedCrawlerState(ArrayList<String> logs) {
        System.out.println(
                "\n>> Singlethreaded crawler state writing started for the file: singlethreadedCrawlerState.txt");
        System.out.println("Writing ");
        for (String line : logs) {
            try {
                this.singlethreadedCrawlerStateFileWriter.write(line);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
        System.out.println(
                ">> Singlethreaded crawler state writing ended for the file:  singlethreadedCrawlerState.txt");
    }

    public void startMultithreadedWebCrawler() {
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::: Multithreaded Web Crawler ::::::::::::::::::::::::::::::::::");
        for (int i = 0; i < this.URLs.length; i++) {
            createThreads();
            ArrayList<String> logs = multiThreadedCrawl(URLs[i]);
            writeMultithreadedWebCrawlerState(logs);
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
            ArrayList<String> logs = singleThreadedCrawl(URLs[i]);
            writeSinglethreadedCrawlerState(logs);
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

    private ArrayList<String> singleThreadedCrawl(String rootURL) {
        ArrayList<String> logs = new ArrayList<>();
        int depth = 0;
        this.urlsQueue.add(rootURL);
        this.visitedURLs.add(rootURL);

        System.out.println("\n>> Web crawling starting at rootURL: " + rootURL);
        System.out.println("Crawling...");

        logs.add(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
        logs.add("\nrootURL: " + rootURL + "\n");

        while (!this.urlsQueue.isEmpty() && depth < this.maxDepth) {
            logs.add("\nlevel: " + depth + "\n");
            int currentLevelSize = this.urlsQueue.size();
            for (int i = 0; i < currentLevelSize; i++) {
                String currentURL = this.urlsQueue.poll();
                logs.add("\t" + currentURL + "\n");
                Document doc = request(currentURL);
                if (doc != null) {
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
        System.out.println(">> Web crawling ended for rootURL: " + rootURL);
        System.out.println("\n>> Analytics <<\n");
        logs.add(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        return logs;
    }

    private ArrayList<String> multiThreadedCrawl(String rootURL) {
        ArrayList<String> logs = new ArrayList<>();
        int depth = 0;
        int childThreadDocProcessedCount = 0;
        int mainThreadDocProcessedCount = 0;
        int threadsRunBreakpoint = 0;

        this.urlsQueue.add(rootURL);
        this.visitedURLs.add(rootURL);

        System.out.println("\n>> Web crawling starting at rootURL: " + rootURL);
        System.out.println("Crawling...");

        logs.add(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
        logs.add("\nrootURL: " + rootURL + "\n");

        while (!this.urlsQueue.isEmpty() && depth < this.maxDepth) {
            logs.add("\nlevel: " + depth + "\n");
            int currentLevelSize = this.urlsQueue.size();
            for (int i = 0; i < currentLevelSize; i++) {
                String currentURL = this.urlsQueue.poll();
                logs.add("\t" + currentURL + "\n");
                Document doc;
                if (this.urlDocMap.containsKey(currentURL)) {
                    doc = this.urlDocMap.get(currentURL);
                    childThreadDocProcessedCount++;
                } else {
                    doc = request(currentURL);
                    mainThreadDocProcessedCount++;
                }
                if (doc != null) {
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
        System.out.println(">> Web crawling ended for rootURL: " + rootURL);
        System.out.println("\n>> Analytics <<\n");
        System.out.println("mainThreadDocProcessedCount: " + mainThreadDocProcessedCount
                + "\nchildThreadDocProcessedCount: " + childThreadDocProcessedCount + "\n");
        logs.add(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        return logs;
    }
}