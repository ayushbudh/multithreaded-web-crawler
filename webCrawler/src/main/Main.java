package main;

// Main class is designed to test the webcrawler
public class Main {
	public static void main(String[] args) {
		int maxDepth = 2;
		int numThreads = 2;
		String URLS[] = { "https://dictionary.com" };

		SequentialWebCrawler sequentialWebCrawler = new SequentialWebCrawler(URLS, maxDepth);
		sequentialWebCrawler.startCrawler();

		MultithreadedWebCrawler multithreadedWebCrawler = new MultithreadedWebCrawler(URLS, maxDepth, numThreads);
		multithreadedWebCrawler.startCrawler();

	}
}
