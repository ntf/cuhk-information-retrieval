# -*- coding: utf-8 -*-
# filename: crawler.py

import sqlite3  
import urllib2  
from HTMLParser import HTMLParser  
from urlparse import urlparse
import xml.sax
import csv





class commentHandler( xml.sax.ContentHandler):
    def __init__(self):
        self.CurrentData = ""
        self.title = ""
        self.link = ""
        self.guid = ""
        self.pubDate = ""
        self.dcDate = ""
        self.description = ""
        self.media_title = ""
        self.media_thumbnail = ""
        self.counter = 0
        self.result = ""

    def startElement(self, tag, attributes):
        self.CurrentData = tag
#        if tag == "item":
#           print "\n\n\n"
#           print "******item******"
        if tag == "media:thumbnail":
            print "attributes: ", attributes['url']
            self.media_thumbnail = attributes['url']
        if tag == "guid":
            if attributes['isPermaLink'] != "true":
                tag = ""

#       elif tag == "dc:date":
#           print "dc:date"

#           title = attributes["item"]
#           print ""
    def endElement(self, tag):
#        if self.CurrentData == "title":
#            print "Title:", self.title
#        elif self.CurrentData == "link":
#            print "link:", self.link
#        elif self.CurrentData == "guid":
#            print "guid:", self.guid
#        elif self.CurrentData == "dc:date":
#            print "dc:Date:", self.dcDate
#        elif self.CurrentData == "pubDate":
#            print "pubDate:", self.pubDate
#        elif self.CurrentData == "description":
#            print "description:", self.description
#        elif self.CurrentData == "media:title":
#            print "media_title:", self.media_title
#       elif self.CurrentData == "media:thumbnail":
#           print "media_thumbnail:", self.media_thumbnail
        self.counter += 1
        self.CurrentData = ""

    def characters(self, content):
        if self.CurrentData == "title":
            self.title = content
        elif self.CurrentData == "link":
            self.link = content
        elif self.CurrentData == "guid":
            self.guid = content
        elif self.CurrentData == "dc:date":
            self.dcDate = content
        elif self.CurrentData == "pubDate":
            self.pubDate = content
            if self.counter < 20:
                self.result += content
#               self.result += "\n"
        elif self.CurrentData == "description":
            self.description = content
            self.result += content
            self.result += "\n"
        elif self.CurrentData == "media:title":
            self.media_title = content
        elif self.CurrentData == "media:thumbnail":
            self.media_thumbnail = content














class HREFParser(HTMLParser):  
    """
    Parser that extracts hrefs
    """
    hrefs = set()
    def handle_starttag(self, tag, attrs):
        if tag == 'a':
            dict_attrs = dict(attrs)
            if dict_attrs.get('href'):
                if dict_attrs.get('class'):
                    if dict_attrs['class'] == "comments may-blank":
#                        print "comment found"
                        self.hrefs.add(dict_attrs['href'])


def get_local_links(html, domain, depth):  
    """
    Read through HTML content and returns a tuple of links
    internal to the given domain
    """
    hrefs = set()
    parser = HREFParser()
    parser.feed(html)
 #   print "parser: {}".format(parser)
    for href in parser.hrefs:
#        print "href: ", href
#        print "{}".format(href)
        u_parse = urlparse(href)
#        print "u_parse:", u_parse.path
        if href.startswith('/'):
            # purposefully using path, no query, no hash
            hrefs.add(u_parse.path)
        else:
          # only keep the local urls
          
          if u_parse.netloc == domain:
            hrefs.add(u_parse.path)
    return hrefs


class CrawlerCache(object):  
    """
    Crawler data caching per relative URL and domain.
    """
    def __init__(self, db_file):
        self.conn = sqlite3.connect(db_file)
        c = self.conn.cursor()
        c.execute('''CREATE TABLE IF NOT EXISTS sites
            (domain text, url text, content text)''')
        self.conn.commit()
        self.cursor = self.conn.cursor()

    def set(self, domain, url, data):
        """
        store the content for a given domain and relative url
        """
#        self.cursor.execute("INSERT INTO sites VALUES (?,?,?)",
#            (domain, url, data))
#        self.conn.commit()

    def get(self, domain, url):
        """
        return the content for a given domain and relative url
        """
#        self.cursor.execute("SELECT content FROM sites WHERE domain=? and url=?",
#            (domain, url))
#        row = self.cursor.fetchone()
#        if row:
#            return row[0]

    def get_urls(self, domain):
        """
        return all the URLS within a domain
        """
#        self.cursor.execute("SELECT url FROM sites WHERE domain=?", (domain,))
        # could use fetchone and yield but I want to release
        # my cursor after the call. I could have create a new cursor tho.
        # ...Oh well
#        return [row[0] for row in self.cursor.fetchall()]


class Crawler(object):  
    def __init__(self, cache=None, depth=2):
        """
        depth: how many time it will bounce from page one (optional)
        cache: a basic cache controller (optional)
        """
        self.depth = depth
        self.content = {}
        self.cache = cache

    def crawl(self, url, no_cache=None):
 
        u_parse = urlparse(url)
        self.domain = u_parse.netloc
        self.content[self.domain] = {}
        self.scheme = u_parse.scheme
        self.no_cache = no_cache
        self._crawl([u_parse.path], self.depth)

    def set(self, url, html):
        self.content[self.domain][url] = html
        if self.is_cacheable(url):
#             print "hihi\n"
#            print "The website: {}".format(html)
            parser = xml.sax.make_parser()
            # turn off namepsaces
            parser.setFeature(xml.sax.handler.feature_namespaces, 0)

            # override the default ContextHandler
            Handler = commentHandler()
            parser.setContentHandler( Handler )
            try:
                req = urllib2.Request('%s://%s%s' % (self.scheme, self.domain, url))
                toursurl = urllib2.urlopen(req)
#            parser.parseString(html)
                parser.parse(toursurl)
                print "guid: ",Handler.guid
#                print "tokens: ",url.split('/',5)
                subreddit = url.split('/',5)[2]
                post_id = url.split('/',5)[4]
                print "subreddit: ",subreddit
                subreddit += ".csv"
                print "post_id: ",post_id
                resultFile = open(subreddit,'a')
                wr = csv.writer(resultFile, dialect='excel')
                OUTPUT = []
                OUTPUT.append(post_id)
                OUTPUT.append(Handler.title)
                OUTPUT.append(Handler.pubDate)
                OUTPUT.append(Handler.link)
                result = Handler.result.encode('utf-8')
#                output_result = result.encode('latin-1', 'ignore')
                OUTPUT.append(result)
                wr.writerow(OUTPUT)

            except urllib2.HTTPError, e:
                print "error [%s] %s: %s" % (self.domain, url, e)
                self.set(url, html)



#            self.cache.set(self.domain, url, html)

    def get(self, url):
        page = None
        if self.is_cacheable(url):
          page = self.cache.get(self.domain, url)
        if page is None:
          page = self.curl(url)
        else:
          print "cached url... [%s] %s" % (self.domain, url)
        return page

    def is_cacheable(self, url):
        return self.cache and self.no_cache \
            and not self.no_cache(url)

    def _crawl(self, urls, max_depth):
        n_urls = set()
        if max_depth:
            for url in urls:
                # do not crawl twice the same page
                html = ""
                if max_depth == 1:
                    url += ".xml"
#                print "url: ",url
                if url not in self.content:
                    if max_depth != 1:
                        html = self.get(url)
                    if max_depth == 1:
                        self.set(url, html)
                    n_urls = n_urls.union(get_local_links(html, self.domain, max_depth))
            self._crawl(n_urls, max_depth-1)


    def curl(self, url):
        """
        return content at url.
        return empty string if response raise an HTTPError (not found, 500...)
        """
        try:
            print "retrieving url... [%s] %s" % (self.domain, url)
            req = urllib2.Request('%s://%s%s' % (self.scheme, self.domain, url))
            response = urllib2.urlopen(req)
            return response.read().decode('ascii', 'ignore')
        except urllib2.HTTPError, e:
            print "error [%s] %s: %s" % (self.domain, url, e)
            return ''