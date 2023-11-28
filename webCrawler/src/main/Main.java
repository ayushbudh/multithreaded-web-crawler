package main;

public class Main {
	public static void main(String args[]) {
		String URLS[] = { "http://books.toscrape.com", "https://dictionary.com" };
		int noOfThreads = 3;
		int maxDepth = 2;

		WebCrawler webCrawler = new WebCrawler(URLS, noOfThreads, maxDepth);

		webCrawler.startMultithreadedWebCrawler();
		webCrawler.startSinglethreadedWebCrawler();
	}
}