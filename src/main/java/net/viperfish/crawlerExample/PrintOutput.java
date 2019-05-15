package net.viperfish.crawlerExample;

import net.viperfish.crawler.core.Datasink;
import net.viperfish.crawler.html.CrawledData;

import java.io.IOException;

public class PrintOutput implements Datasink<CrawledData> {

    private boolean closed;

    @Override
    public void init() throws IOException {
        closed = false;
    }

    @Override
    public synchronized void write(CrawledData data) throws IOException {
        System.out.println("Crawling:" + data.getTitle());
        System.out.println("Url:" + data.getUrl().toString());
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }
}
