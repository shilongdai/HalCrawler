[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)

# HalCrawler
## Overview
The HalCrawler project is the first in a series of planned projects that will hopefully lead to a working search engine that returns reasonable results. It provides a framework for creating a webcrawler, both generic and specific, to scrape contents off of various webpages. The end goal of the project is to have a fully modular template for creating specific crawlers that are tailored to various tasks such as fetching text for index, getting data from tables, scanning through forum threads for comments and memes, or downloading all versions of product documentations.

## Status
Currently, the crawler is able to follow the links through various websites and interpret the html results based on supplied customized modules. It respects robots.txt and have tunable throttle controls. The output of the crawling can now be customized by various processors to have different attributes. It is ready to be applied to the search engine.


## Details
### Structure
The crawler framework has five main components:

- The base crawler
- The content processors
- The content fetcher
- The storage backend
- The restriction manager

The base crawler is the master controller of all the other components. It accepts URLs from external sources to craw, uses the content fetcher to fetch the resources referenced by the URLs, processes the contents with various content processors, and store the crawled results into the storage backend.

Each content processor is matched to a specific section in the fetched content. In HTML, that would be sections identified by various structural informations. The content processors are responsible for processing their designated sections that are meaningful to the context of the crawler.

The content fetcher fetches web pages by URL. It deals with all the raw information such as encoding, rate limits, mime type etc specific to the sites. It is responsible to produce the raw data readable by the processors.

The storage backend is the data layer of the crawler. It stores all the processed resources that the crawler have crawled.

The restriction manager checks the restrictions for crawling the site. It controls whether the crawler will hit specific pages on a site. For instance, it would ensure that the crawler does not go to any pages that matches the disallowed section of the robots.txt file.
