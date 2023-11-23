package main;

import java.io.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MultithreadedWebCrawler implements Runnable {

    private int maxDepth;
    private int numThreads;
    private String URLS[];
    private Set<String> visitedLinks;
    private String stateFilePath = "crawlerState.txt";

    // constructor
    public MultithreadedWebCrawler(String URLS[], int maxDepth, int numThreads) {
        System.out.println("WebCrawler created");
        // initializes the visitedLinks set and uses collections.synchronizedSet to make
        // the set thread-safe
        this.visitedLinks = Collections.synchronizedSet(new HashSet<>());
        this.URLS = URLS;
        this.numThreads = numThreads;
        this.maxDepth = maxDepth;
    }

    public void startCrawler() {
        // loads the previously visited links from the state file, updating the
        // visitedLinks set
        loadState();

        // an array of thread objects is created to manage multiple threads
        Thread[] threads = new Thread[numThreads];
        // for each thread a new Thread object is created with the current instance of
        // Bot
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(this);
            // initiates the execution of the thread which invokes the run method
            threads[i].start();
        }
        // this loop iterates over each thread in the array and overall ensures that the
        // program waits for all threads
        for (Thread thread : threads) {
            try {
                // waits for the thread to complete before moving on to the next one
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // saves the current state of visitedLinks to the stateFile
        saveState();
        printVisitedLinks(); // prints final set of visited links
    }

    @Override
    // invoked when a Thread is started
    public void run() {
        // crawl method is called with initial depth:0 and the startLink
        for (String URLString : this.URLS) {
            crawl(0, URLString);
        }
    }

    // crawl method is the core logic of web crawler
    // synchronized keyword ensures that only one thread can execute this method at
    // a time which makes it thread safe
    private void crawl(int level, String url) {
        // condition that checks whether the current depth is within the specified
        // maximum depth and if the current URL has not been visited
        if (level <= maxDepth && !visitedLinks.contains(url)) {
            /*
             * using the Jsoup library the request method retrieves the web page content as
             * a Document object
             * this method makes an HTTP request to the given URL and returns the parsed
             * HTML document
             */
            Document doc = request(url);
            // condition that checks if the document is retrieved
            if (doc != null) {
                /*
                 * method proceeds if document is successfully retrieved
                 * this loop iterated over each link found on page using Jsoup select
                 * the select(a[href]) is used to select <a> elements with and href attribute
                 */
                for (Element link : doc.select("a[href]")) {
                    // for each link the absolute URL is extracted
                    String next_link = link.absUrl("href");
                    // crawl method is recursively called with an incremented depth and the next
                    // link
                    crawl(level + 1, next_link);
                }
                visitedLinks.add(url); // Add the current URL to the set of visited links.
            }
        }
    }

    private Document request(String url) {
        // try catch block that allows the program to handle potential errors
        try {
            // creates a connection to the specified URL using the Jsoup library
            Connection con = Jsoup.connect(url);
            /*
             * the get method sends an HTTP GET request to the URL and parses the response
             * into a Document object
             * so basically the get method retrieves the HTML documents from the connected
             * URL
             */
            Document doc = con.get();
            // condition that checks if the HTTP request is successful
            if (con.response().statusCode() == 200) {
                // message is printed that shows that the web page was successfully received by
                // the bot
                System.out.println("\n Thread ID:" + Thread.currentThread().getId() + " Received Webpage at " + url);
                // retrieves and prints the tile of the webpage
                String title = doc.title();
                System.out.println(title);
                // visitedLinks.add(url);
                // method returns parsed HTML document
                return doc;
            }
            // if condition is not met then the method returns null
            return null;
            // if IOexception then return null
        } catch (IOException e) {
            return null;
        }
    }

    private void loadState() {
        try {
            // creates a Path object representing the file path 'stateFilePath'
            Path filePath = Path.of(stateFilePath);
            // condition that checks to see if 'filePath' exists
            if (Files.exists(filePath)) {
                // reads all lines from the file and returns them as a list and the list is used
                // to create a hashset names loadedLinks
                Set<String> loadedLinks = new HashSet<>(Files.readAllLines(filePath));
                // the loadedLinks elements are added to the existing visitedLinks set which
                // ensures that the bot's visitedLinks is updated with the loaded data
                visitedLinks.addAll(loadedLinks);
            }
        } catch (IOException e) {
            System.err.println("Error loading state: " + e.getMessage());
        }

    }

    private void saveState() {
        try {
            // creates a Path object representing the file path 'stateFilePath'
            Path filePath = Path.of(stateFilePath);
            // is used to write contents of the visitedLinks to the file
            Files.write(filePath, visitedLinks);
        } catch (IOException e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }

    private void printVisitedLinks() {
        System.out.println("Final visited links: " + visitedLinks);
    }

    public Set<String> getVisitedLinks() {
        return Collections.unmodifiableSet(visitedLinks);
    }

    public int getNumThreads() {
        return numThreads;
    }

}
