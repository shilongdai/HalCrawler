package net.viperfish.crawlerExample;

import net.viperfish.crawler.html.HttpFetcher;
import net.viperfish.crawler.html.HttpWebCrawler;
import net.viperfish.crawler.html.ThreadPoolHttpWebCrawler;
import net.viperfish.crawler.html.crawlHandler.BaseInMemCrawlChecker;
import net.viperfish.crawler.html.crawlHandler.CascadingPriorityBooster;
import net.viperfish.crawler.html.crawlHandler.Limit2HostHandler;
import net.viperfish.crawler.html.engine.ApplicationPrioritizedConcurrentHttpFetcher;
import net.viperfish.crawler.html.restrictions.RobotsTxtRestrictionManager;

import java.net.MalformedURLException;
import java.net.URL;

public class CrawlLanding {

    public static void main(String[] argv) throws MalformedURLException {
        URL url2Test = new URL("https://www.whitehouse.gov/");
        HttpFetcher fetcher = new ApplicationPrioritizedConcurrentHttpFetcher(1);
        RobotsTxtRestrictionManager robotsTxtRestrictionManager = new RobotsTxtRestrictionManager("halbot");
        fetcher.registerRestrictionManager(robotsTxtRestrictionManager);
        try {
            fetcher.init();
        } catch (Exception e) {
            System.out.println("Cannot initialize fetcher:" + e.getMessage());
            // if(e instanceof xxx) ... or use the specific subclass with the specific exception when initializing
            e.printStackTrace();
            return;
        }
        PrintOutput output = new PrintOutput();
        try {
            output.init();
        } catch (Exception e) {
            System.out.println("Cannot initialize output:" + e.getMessage());
            // if(e instanceof xxx) ... or use the specific subclass with the specific exception when initializing
            e.printStackTrace();
        }
        HttpWebCrawler crawler = new ThreadPoolHttpWebCrawler(8, output,
                fetcher);
        crawler.registerCrawlerHandler(new Limit2HostHandler("www.whitehouse.gov"));
        crawler.registerCrawlerHandler(new BaseInMemCrawlChecker());
        CascadingPriorityBooster booster = new CascadingPriorityBooster(50, 0.75);
        crawler.registerCrawlerHandler(booster);
        crawler.submit(url2Test);
        crawler.startProcessing();
        try {
            crawler.waitUntiDone();
        } catch (InterruptedException e) {
            System.out.println("Crawling was interrupted");
        }
        crawler.shutdown();
        try {
            fetcher.close();
        } catch (Exception e) {
            System.out.println("Failed to cleanup fetcher");
        }
    }

}
