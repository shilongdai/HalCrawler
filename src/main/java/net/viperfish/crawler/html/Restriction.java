package net.viperfish.crawler.html;

public interface Restriction {

	boolean canIndex();

	boolean canFetch();
}
