package main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BotTest {

	 @Test
	    void testCrawlerWithResume() {
	        // Create an instance of Bot
	        Bot bot = new Bot("https://dictionary.com", 2, 3);
	        bot.startBot();
	        // Create a new instance of Bot for resuming
	        Bot resumedBot = new Bot("https://dictionary.com", 2, 3);
	        // Start the resumed bot, which should resume from the previous state
	        resumedBot.startBot();
	        // Check if the two instances have visited the same number of links
	        assertEquals(bot.getVisitedLinks().size(), resumedBot.getVisitedLinks().size());
	    }

	    @Test
	    void testAppropriateNumberOfThreads() {
	        // Create an instance of Bots
	        Bot bot = new Bot("https://dictionary.com", 2, 3);
	        bot.startBot();
	        // Check if the number of threads used by the bot matches the specified number
	        assertEquals(3, bot.getNumThreads());
	    }

	    @Test
	    void testWebCrawler() {
	        // Create an instance of Bot
	        Bot bot = new Bot("https://dictionary.com", 2, 3);
	        bot.startBot();

	        // Check if the visited links set is not empty
	        assertFalse(bot.getVisitedLinks().isEmpty());
	    }
	}