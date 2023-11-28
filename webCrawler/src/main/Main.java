package main;

public class Main {
	private static class ShutDownTask extends Thread {
		private WebCrawler webCrawler;

		public ShutDownTask(WebCrawler webCrawler) {
			this.webCrawler = webCrawler;
		}

		@Override
		public void run() {
			System.out.println(">> Performing shutdown");
			System.out.println("Cleaning resources ...");
			webCrawler.releaseResources();
			System.out.println("Resources cleaned!\n");
		}
	}

	public static void main(String args[]) {
		String URLS[] = { "https://dictionary.com", "http://books.toscrape.com" };
		int noOfThreads = 3;
		int maxDepth = 2;

		WebCrawler webCrawler = new WebCrawler(URLS, noOfThreads, maxDepth);
		ShutDownTask shutDownTask = new ShutDownTask(webCrawler);

		// add shutdown hook to clean resources during program termination
		Runtime.getRuntime().addShutdownHook(shutDownTask);

		webCrawler.startMultithreadedWebCrawler();
		webCrawler.startSinglethreadedWebCrawler();
	}
}