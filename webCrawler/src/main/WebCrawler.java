package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.ArrayList;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// This class can be used to create multiple single and multi threaded web crawler instances
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

    /*
     * This class can be used to created multiple threads for
     * processing webpage content
     */
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
    }

    /*
     * This method is used to perform clean up before both unexpected and
     * expected shutdown of JVM while running this program.
     * Case 1: Unexpected termination of program using Ctrl + C command
     * Case 2: Expected termination of program when the execution is complete
     */
    public void releaseResources() {
        clearState();
        stopThreads();
        for (int i = 0; i < this.multithreadedCrawlerStateFileWriters.length; i++) {
            closeMultithreadedWebCrawlerStateWriter(i);
        }
        for (int i = 0; i < this.singlethreadedCrawlerStateFileWriters.length; i++) {
            closeSinglethreadedWebCrawlerStateWriter(i);
        }
    }

    // This is a helper method used to create file writer
    private void createMultithreadedWebCrawlerFileWriter(int idx) {
        try {
            this.multithreadedCrawlerStateFileWriters[idx] = new FileWriter(
                    pathToDataDir + "/data/multithreadedCrawler/root_url_" + (idx + 1) + ".txt", true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // This is a helper method used to create file writer
    private void createSinglethreadedWebCrawlerFileWriter(int idx) {
        try {
            this.singlethreadedCrawlerStateFileWriters[idx] = new FileWriter(
                    pathToDataDir + "/data/singlethreadedCrawler/root_url_" + (idx + 1) + ".txt", true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // This is a helper method used to create threads
    private void createThreads() {
        for (int i = 0; i < this.noOfThreads; i++) {
            threads[i] = new DocumentDownloadThread("thread_" + i, this.urlsDocProcessingQueue, this.urlDocMap);
        }
    }

    // This is a helper method used to start threads
    private void startThreads() {
        for (int i = 0; i < this.noOfThreads; i++) {
            threads[i].start();
        }
    }

    // This is a helper method used to stop and clean up threads
    private void stopThreads() {
        for (int i = 0; i < this.noOfThreads; i++) {
            try {
                if (threads[i] != null) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < this.noOfThreads; i++) {
            threads[i] = null;
        }
    }

    // This is a helper method used to clear data structures
    private void clearState() {
        this.urlsQueue.clear();
        this.urlsDocProcessingQueue.clear();
        this.urlDocMap.clear();
    }

    // This is a helper method used to write to files
    private void writeSinglethreadedCrawlerState(String line, int idx) {
        try {
            this.singlethreadedCrawlerStateFileWriters[idx].write(line);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // This is a helper method used to write to files
    private void writeMultithreadedWebCrawlerState(String line, int idx) {
        try {
            this.multithreadedCrawlerStateFileWriters[idx].write(line);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // This is a helper method used to close file writer
    private void closeSinglethreadedWebCrawlerStateWriter(int idx) {
        try {
            if (this.singlethreadedCrawlerStateFileWriters[idx] != null) {
                this.singlethreadedCrawlerStateFileWriters[idx].close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // This is a helper method used to close file writer
    private void closeMultithreadedWebCrawlerStateWriter(int idx) {
        try {
            if (this.multithreadedCrawlerStateFileWriters[idx] != null) {
                this.multithreadedCrawlerStateFileWriters[idx].close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    /*
     * This is a helper method used to get number of files in the
     * /data/multithreadedCrawler directory
     */
    private int getNumberOfFilesInDataCrawlerDir(String folderName) {
        File directory = new File(pathToDataDir + folderName);
        return directory.list().length;
    }

    // This is a helper method used to get text based on the matcher
    private static String extractValue(Matcher matcher) {
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return "Not found";
        }
    }

    /*
     * This is a helper method used to get the processed lines which don't need to
     * be processed again during resuming execution of the program
     * 
     * For ex: If Level 0 and Level 1 are processed completely (i.e. for these URLs,
     * there document has been downloaded) but Level 2's URLs are partially
     * processed then we print
     * the Level 0 and Level 1 by using the state from the file and start with
     * processing the chidlren of the Level 1. This helps to save extra computation
     * by using the previous written data to the file.
     */
    private ArrayList<String> getCompleteLines(int fileIdx, int level) {
        String regex = "Level: " + level;
        Pattern pattern = Pattern.compile(regex);
        ArrayList<String> completeLines = new ArrayList<>();
        boolean removeLines = false;
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(pathToDataDir + "/data/multithreadedCrawler/root_url_" + fileIdx + ".txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    removeLines = true;
                    break;
                }
                if (!removeLines && !line.trim().isEmpty()) {
                    completeLines.add(line);
                    completeLines.add("\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        return completeLines;
    }

    // This is a helper method to empty the file content
    private void emptyFile(int fileIdx) {
        try {
            Path path = Path.of(pathToDataDir + "/data/multithreadedCrawler/root_url_" + fileIdx + ".txt");
            Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING).close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    /*
     * This is a helper method to delete the data dir files for the multithreaded
     * folder. This is used to clear directory when the program has already
     * completed processing all the URLs and no more URLs are available to be
     * processed. Triggering this function indicates the re-running of the program
     * to process all the URLs
     */
    private void deleteMultithreadedDataDirectoryFiles() {
        System.out.println("\n>> Clear directory");
        System.out.println("deleting files...");
        File dir = new File(pathToDataDir + "/data/multithreadedCrawler/");
        System.out.println("Deleted files: ");
        for (File file : dir.listFiles()) {
            try {
                if (file.delete()) {
                    System.out.print(file.getName() + ", ");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("\n>> Directory cleared");
    }

    /*
     * This method helps to process the current multithreaded crawler state from the
     * previous program runs.
     * This method performs two actions:
     * 1. Reading and parsing current multithreaded crawler state based on all the
     * previous program runs.
     * 2. Resumes execution based on the the read state in step 1.
     * 
     * Note: For more info, refer to the termination_states directory to view all
     * possible termination states for this webcrawler
     */
    private void processCurrentMultithreadedWebCrawlerState(int fileIdx) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new FileReader(
                            pathToDataDir + "/data/multithreadedCrawler/root_url_" + fileIdx + ".txt"));
            String line = reader.readLine();
            String fileContent = "";
            int totalURLsProcessedOnaFile = 0, urlCount = 0;
            ArrayList<FileData> urlExpectedActualList = new ArrayList<>();
            while (line != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.startsWith("Level:")) {
                        if (urlExpectedActualList.size() > 0) {
                            urlExpectedActualList.get(urlExpectedActualList.size() - 1)
                                    .setExpectedURLsCount(urlCount);
                        }
                        Pattern levelPattern = Pattern.compile("Level: (\\d+)");
                        Pattern urlsPattern = Pattern.compile("No. of URLs: (\\d+)");
                        Pattern urlPattern = Pattern.compile("Current Level URL: (.+)");

                        Matcher levelMatcher = levelPattern.matcher(line);
                        Matcher urlsMatcher = urlsPattern.matcher(line);
                        Matcher urlMatcher = urlPattern.matcher(line);

                        int level = Integer.parseInt(extractValue(levelMatcher));
                        int numberOfURLs = Integer.parseInt(extractValue(urlsMatcher));
                        String currentLevelURL = extractValue(urlMatcher);
                        urlExpectedActualList.add(new FileData(-1, numberOfURLs, currentLevelURL, level));
                        urlCount = 0;
                    } else if (!line.startsWith("Root URL:") && line.startsWith("http")) {
                        totalURLsProcessedOnaFile = totalURLsProcessedOnaFile + 1;
                        this.visitedURLs.add(line);
                        urlCount++;
                    }
                }
                fileContent += line;
                line = reader.readLine();
            }
            reader.close();
            resumeFromCurrentMultithreadedWebCrawlerState(fileContent, totalURLsProcessedOnaFile, urlCount, fileIdx,
                    urlExpectedActualList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * This method helps to resume multithreaded web crawler based on the current
     * state.
     */
    private void resumeFromCurrentMultithreadedWebCrawlerState(String fileContent, int totalURLsProcessedOnaFile,
            int urlCount, int i,
            ArrayList<FileData> urlExpectedActualList) {

        // file was created but is empty -> resume the crawler from the rootURL for file
        if (fileContent.equals("")) {
            createThreads();
            System.out.println("\n>> Resuming previous execution...");
            multiThreadedCrawl(this.URLs[i - 1], i - 1, 0, true);
            clearState();
            stopThreads();
        }
        // if totalURLsProcessedOnaFile == 0 -> empty file is created with no content
        // if totalURLsProcessedOnaFile == 1 -> only Root URL: URL line is present in
        // the file
        if (totalURLsProcessedOnaFile == 0 || totalURLsProcessedOnaFile == 1) {
            createThreads();
            System.out.println("\n>> Resuming previous execution...");
            multiThreadedCrawl(this.URLs[i - 1], i - 1, 0, false);
            clearState();
            stopThreads();
        }
        // update the last urlCount read from the file
        if (urlCount != 0) {
            urlExpectedActualList.get(urlExpectedActualList.size() - 1).setExpectedURLsCount(urlCount);
        }

        /*
         * Check if the actualURLCount from the file matches the Current Level URL count
         * in the text file (root_url_n.txt):
         * This helps to determine if the number of URLs that we were suppose to
         * print/process for the text file (root_url_n.txt)
         * were processed or not and based on that we go one level above and start
         * processing the children of that level.
         * 
         * Note: Going backwards is more efficient since files are created/written in
         * increasing order (i.e. 1,2,3...n)
         */
        int j = urlExpectedActualList.size() - 1;
        while (j >= 0) {
            if (urlExpectedActualList.get(j).getActualURLsCount() != urlExpectedActualList.get(j)
                    .getExpectedURLsCount()) {
                createThreads();
                System.out.println("\n>> Resuming previous execution...");
                ArrayList<String> completeLines = getCompleteLines(i,
                        urlExpectedActualList.get(j - 1).getLevel());
                emptyFile(i);
                createMultithreadedWebCrawlerFileWriter(i - 1);
                for (String completeLine : completeLines) {
                    writeMultithreadedWebCrawlerState(completeLine, i - 1);
                }
                closeMultithreadedWebCrawlerStateWriter(i - 1);
                multiThreadedCrawl(urlExpectedActualList.get(j - 1).getURL(), i - 1,
                        urlExpectedActualList.get(j - 1).getLevel(), false);
                clearState();
                stopThreads();
                for (int k = i; k < this.URLs.length; k++) {
                    createThreads();
                    multiThreadedCrawl(URLs[k], k, 0, true);
                    clearState();
                    stopThreads();
                }
                break;
            }
            j--;
        }
        // Restart URLs processing: clear the data directory and process the URLs again
        if (j == -1) {
            // TODO: stopThreads shouldn't be needed to called here.
            stopThreads();
            deleteMultithreadedDataDirectoryFiles();
            clearState();
            System.out.println("\n>> New execution...");
            this.visitedURLs.clear();
            for (int k = 0; k < this.URLs.length; k++) {
                createThreads();
                multiThreadedCrawl(URLs[k], k, 0, true);
                clearState();
                stopThreads();
            }
        }
    }

    // This is the entry point method for the multithreaded web crawler
    public void startMultithreadedWebCrawler() {
        int numberOfFiles = getNumberOfFilesInDataCrawlerDir("/data/multithreadedCrawler");
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::: Multithreaded Web Crawler ::::::::::::::::::::::::::::::::::");
        if (numberOfFiles == 0) {
            System.out.println("\n>> New execution...");
            this.visitedURLs.clear();
            for (int i = 0; i < this.URLs.length; i++) {
                createThreads();
                multiThreadedCrawl(URLs[i], i, 0, true);
                clearState();
                stopThreads();
            }
        } else {
            processCurrentMultithreadedWebCrawlerState(numberOfFiles);
        }
        System.out.println(
                "\n:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n");
    }

    // This is the entry point method for the singlethreaded web crawler (this
    // method was just used for testing purpose)
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

    // This is a helper method to make requests to Jsoup API to get the HTML
    // document
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

    /*
     * This method helps to crawl the URL Graph for a rootURL. It uses
     * Breadth First Search (BFS) algorithm to process every level in the URL Graph.
     * 
     * Note: For URL Graph, refer to example of URL Graph in README.md of this repo.
     */
    private void singleThreadedCrawl(String rootURL, int idx) {
        int depth = 0;
        this.urlsQueue.add(rootURL);
        this.visitedURLs.add(rootURL);

        System.out.println("\n>> Web crawling starting at rootURL: " + rootURL);
        System.out.println("Crawling...");

        createSinglethreadedWebCrawlerFileWriter(idx);
        writeSinglethreadedCrawlerState("\nRoot URL: " + rootURL + "\n", idx);

        long startTime = System.currentTimeMillis();

        while (!this.urlsQueue.isEmpty() && depth < this.maxDepth) {
            int currentLevelSize = this.urlsQueue.size();
            writeSinglethreadedCrawlerState("\nLevel: " + depth + ", No. of URLs: " + currentLevelSize + "\n", idx);
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
        closeSinglethreadedWebCrawlerStateWriter(idx);
    }

    /*
     * This method helps to crawl the URL Graph for a rootURL. It uses
     * Breadth First Search (BFS) algorithm to process every level in the URL Graph.
     * 
     * It spawns the threads when the threadsRunBreakpoint == available threads ->
     * this helps to starts threads execution only when the program has enough work
     * to do. Hence, saving resources.
     * 
     * For Analytics, this method prints the number of URLs proceesed by both main
     * and child threads for this program. In cases where no. of URLs to be
     * processed >= no. of threads -> no. of URLs processed by child threads >> no.
     * of URLs processed by main thread.
     * 
     * Note: For URL Graph, refer to example of URL Graph in README.md of this repo.
     */
    private void multiThreadedCrawl(String rootURL, int idx, int level, boolean isNewWrite) {
        int depth = level;
        int childThreadDocProcessedCount = 0;
        int mainThreadDocProcessedCount = 0;
        int threadsRunBreakpoint = 1;

        System.out.println("\n>> Web crawling starting at rootURL: " + rootURL);
        System.out.println("Crawling...");
        createMultithreadedWebCrawlerFileWriter(idx);

        if (isNewWrite) {
            writeMultithreadedWebCrawlerState("\nRoot URL: " + rootURL + "\n", idx);
        }

        this.urlsQueue.add(rootURL);
        this.visitedURLs.add(rootURL);

        long startTime = System.currentTimeMillis();

        while (!this.urlsQueue.isEmpty() && depth < this.maxDepth) {
            int currentLevelSize = this.urlsQueue.size();
            writeMultithreadedWebCrawlerState("\nLevel: " + depth + ", No. of URLs: " + currentLevelSize
                    + " Current Level URL: " + this.urlsQueue.peek() + "\n", idx);
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
        // TODO: Fix elapsedTime calculation for the cases in partial
        // processing has been done.
        System.out.println(
                "Total time: " + elapsedTime + " ms\nmainThreadDocProcessedCount: " + mainThreadDocProcessedCount
                        + "\nchildThreadDocProcessedCount: " + childThreadDocProcessedCount + "\n");
        closeMultithreadedWebCrawlerStateWriter(idx);
    }
}