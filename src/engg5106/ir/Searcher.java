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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import engg5106.ir.bm25.bm25;
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
		Indexer indexer = new Indexer(new File("index/index100"));
		bm25 scorer = new bm25();
		IndexOptions[] options = new IndexOptions[] {
				// new IndexOptions("subreddit", IndexOptions.Type.Keyword),
				// new IndexOptions("domain", IndexOptions.Type.Keyword),
				new IndexOptions("title", IndexOptions.Type.Tokenize),
				new IndexOptions("content", IndexOptions.Type.Tokenize) };

		// Index configuration , multiple tiers
		indexer.setOptions(options);

		indexer.ready();
		indexer.getIndex().debug();
		System.out.println("Ready");

		Index index = indexer.getIndex();
		String query = "posted office";   // Query Entry
		String field = "content";
		List<String> tokens = index.tokenize(index.getAnalyzer(), query); // Normalized query

		int q_termid;
		
		Set<Integer> doc_to_score = new TreeSet<>();
		HashMap<Integer, Integer> qmap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> postingList = new HashMap<Integer, Integer>();
		
		
		
		for (String querkToken : tokens) {
			
			q_termid = index.getTermId(querkToken);
			if (q_termid >= 0) {
				if (!qmap.containsKey(q_termid)) {
					qmap.put(q_termid, 1);
				}
				else
					qmap.put(q_termid, qmap.get(q_termid) + 1);
				
				postingList = index.getPositingList("content", q_termid);
				if ( postingList == null)
					continue;									
				System.out.println(querkToken + " term id is " + q_termid + ", posting list size = " + postingList.size());
				
				for (Integer docID : postingList.keySet())
				{
					doc_to_score.add(docID);
				}
				
			}
			 else {
				 System.out.println("Token no in indexer : " + querkToken);
				 continue;
			}
		}
		
		
		for (Integer docID : doc_to_score)
		{
			
			double score =scorer.rsv(indexer.getIndex(), qmap, docID, field);
			System.out.println("Doc: "+docID + " get " + score);

		}
		
		System.out.println("--DONE--");
	}
}
