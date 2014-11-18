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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import engg5106.ir.SimilarDocuments.Similarity;
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
	
	static public class Similarity {
		public int docID1;
		public double value;

		public Similarity(int a, double d) {
			this.docID1 = a;
			this.value = d;
		}
	}
	
	
	public static void main(String[] args) throws ClassNotFoundException {
		System.out.println("Searcher");
		Indexer indexer = new Indexer(new File("index/index100-20141118"));
		bm25 scorer = new bm25();
		IndexOptions[] options = new IndexOptions[] {
				// new IndexOptions("subreddit", IndexOptions.Type.Keyword),
				// new IndexOptions("domain", IndexOptions.Type.Keyword),
				new IndexOptions("title", IndexOptions.Type.Tokenize),
				new IndexOptions("content", IndexOptions.Type.Tokenize) };

		// Index configuration , multiple tiers
		indexer.setOptions(options);

		indexer.searchReady();
		indexer.getIndex().debug();
		System.out.println("Ready");

		Index index = indexer.getIndex();
		String query = "posted office boy mama";   // Query Entry
		String field = "title";
		Boolean searchComment = true;
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
				
				postingList = index.getPositingList(field, q_termid);
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
		
		HashMap<Integer,Double> score_map = new HashMap<Integer,Double>();
		
		for (Integer docID : doc_to_score)
		{
			
			double score =scorer.rsv(indexer.getIndex(), qmap, docID, field);
				score_map.put(docID,score);
		}
		System.out.println("-----");
		if (searchComment){ // Need to search with comment
		
			field = "content";
			for (Integer docID : doc_to_score){
				double score =scorer.rsv(indexer.getIndex(), qmap, docID, field);
				if (!score_map.containsKey(docID)) {
					score_map.put(docID,score);
				}
				else
					score_map.put(docID,score_map.get(docID)+score);
			}
		}

		ArrayList<Similarity> result = new ArrayList<Similarity>();
		for (Integer docID : score_map.keySet()) {
				result.add(new Similarity(docID, score_map.get(docID)));
		}
		
		Collections.sort(result, new Comparator<Similarity>() {
			@Override
			public int compare(Similarity first, Similarity second) {

				return -1 * Double.compare(first.value, second.value);
			}
		});

		StringBuffer sb = new StringBuffer("Query result:");
		// TOP 10
		for (int j = 0; j < 10; j++) {
			if (j != 0) {
				sb.append(",");
			}
			sb.append(result.get(j).docID1);

			System.out.println(result.get(j).docID1 + " = "+ result.get(j).value);

		}
		
		System.out.println("--DONE--");
	}
	

}



