#!/usr/bin/python

import sqlite3  
import urllib2  
from HTMLParser import HTMLParser  
from urlparse import urlparse
import xml.sax
import csv
import os


def get_page(url):
	Handler = commentHandler()
	try:
		url_xml = url + '.xml'
		print "extracting %s ..." % (url_xml)
		req = urllib2.Request(url_xml)
		toursurl = urllib2.urlopen(req)
		parser = xml.sax.make_parser()
		# turn off namepsaces
		parser.setFeature(xml.sax.handler.feature_namespaces, 0)

		# override the default ContextHandler
		parser.setContentHandler( Handler )
		try:
			parser.parse(toursurl)
			return Handler
		except xml.sax._exceptions.SAXParseException, e:
			print "error %s: %s" % (url_xml, e)
			Handler.title = ""
			return Handler

	except urllib2.HTTPError, e:
                print "error %s: %s" % (url_xml, e)
                if e.errno == 429:
                	return get_page(url)
                Handler.title = ""
                return Handler

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
#            print "attributes: ", attributes['url']
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
#		if self.CurrentData == "description":
#			print "description: %s" % (self.description)
#			print "counter: ", self.counter
#        elif self.CurrentData == "media:title":
#            print "media_title:", self.media_title
#       elif self.CurrentData == "media:thumbnail":
#           print "media_thumbnail:", self.media_thumbnail
#        self.counter += 1
		if self.CurrentData == "description":
			self.counter += 1
			self.result += '\n'
#		if self.CurrentData == "table":
#			print "table..."
		self.CurrentData = ""

    def characters(self, content):
        if self.CurrentData == "title":
            self.title = content
#        elif self.CurrentData == "link":
#            self.link = content
#        elif self.CurrentData == "guid":
#            self.guid = content
#        elif self.CurrentData == "dc:date":
#            self.dcDate = content
#        elif self.CurrentData == "pubDate":
#            self.pubDate = content
#            if self.counter < 20:
#                self.result += content
#               self.result += "\n"
        elif self.CurrentData == "description":
            self.description = content
#            print "discription: ", self.description
#            print "counter: ", self.counter
            if self.counter < 23:
            	if self.counter >= 2:
       		   		self.result += content
#            	self.result += "\n"
#        elif self.CurrentData == "media:title":
#            self.media_title = content
#        elif self.CurrentData == "media:thumbnail":
#            self.media_thumbnail = content


for subdir, dirs, files in os.walk('./data'): # Get subreddit list from data/
	for i in range(163, 1000): #Only read first 1000 subreddits now 163
#		print files[i]
		current_file = 'data/'+files[i]
		print "Processing %s ... " % (current_file)
		output_file = 'result/'+files[i]
		with open(current_file, 'rb') as f:
			reader = csv.reader(f)
			resultFile = open(output_file,'w+')
			wr = csv.writer(resultFile, dialect='excel')
			wr.writerow(['created_utc', 'score', 'domain', 'id', 'title', 'author', 'ups', 'downs', 'num_comments', 'permalink', 'selftext', 'top_20_comments'])
			for row in reader:
#				print row[9]
				if row[9] != 'permalink':
					the_handler = get_page(row[9])
					if the_handler.title != "":
						wr.writerow([row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9], row[10], the_handler.result.encode('utf-8')])
					else:
						wr.writerow([row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9], row[10], ''])