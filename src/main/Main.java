package src.main;

public class Main {
    // Main class is designed to test the webcrawler
    public static void main(String[] args) {
        int maxDepth = 2;
        int numThreads = 3;

        Bot bot = new Bot("https://dictionary.com", maxDepth, numThreads);

        bot.startBot();
    }
}
