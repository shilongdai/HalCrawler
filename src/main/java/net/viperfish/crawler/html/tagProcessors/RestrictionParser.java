package net.viperfish.crawler.html.tagProcessors;

import java.net.URL;
import net.viperfish.crawler.html.Restriction;

public interface RestrictionParser {

	Restriction getRestriction(URL url);
}
