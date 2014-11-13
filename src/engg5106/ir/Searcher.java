package engg5106.ir;

import org.mapdb.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import engg5106.ir.indexer.Index;
import engg5106.ir.indexer.IndexOptions;

/**
 * Extend this class to search the index
 * 
 * @author NTF
 *
 */
public class Searcher {

	public static void main(String[] args) throws ClassNotFoundException {
		System.out.println("Searcher");
		Indexer indexer = new Indexer(new File("index/index400"));

		// Index configuration , multiple tiers
		indexer.setOptions(new IndexOptions[] {
				// new IndexOptions("subreddit", IndexOptions.Type.Keyword),
				// new IndexOptions("domain", IndexOptions.Type.Keyword),
				new IndexOptions("title", IndexOptions.Type.Tokenize),
				new IndexOptions("content", IndexOptions.Type.Tokenize) });

		indexer.ready();
		indexer.getIndex().debug();
		// indexer.save();

		System.out.println("--DONE--");
	}

}
