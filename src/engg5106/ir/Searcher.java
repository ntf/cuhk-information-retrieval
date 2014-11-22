package engg5106.ir;

import org.mapdb.*;

import java.io.BufferedReader;
import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


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
		//System.out.println("Searcher");
		Indexer indexer = new Indexer(new File("index/index10"));
		bm25 scorer = new bm25();
		IndexOptions[] options = new IndexOptions[] {
				// new IndexOptions("subreddit", IndexOptions.Type.Keyword),
				// new IndexOptions("domain", IndexOptions.Type.Keyword),
				new IndexOptions("title", IndexOptions.Type.Tokenize),
				new IndexOptions("content", IndexOptions.Type.Tokenize) };

		// Index configuration , multiple tiers
		indexer.setOptions(options);

		indexer.searchReady();
		//indexer.getIndex().debug();
		//System.out.println("Ready");

		Index index = indexer.getIndex();
		String query = args[0];   // Query Entry
		String field = "title"; 
		Boolean searchComment = Boolean.valueOf(args[1]);
		int scoreLimit = Integer.parseInt(args[2]);
		int timeLimit = Integer.parseInt(args[3]);
		//August 15-20 of August 2013
		List<String> tokens = index.tokenize(index.getAnalyzer(), query); // Normalized query

		int q_termid;
		
		Set<Integer> doc_to_score = new TreeSet<>();
		HashMap<Integer, Integer> qmap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> postingList = new HashMap<Integer, Integer>();
		long oldtime = 0;
		oldtime = System.nanoTime();
		
		
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
				//System.out.println(querkToken + " term id is " + q_termid + ", posting list size = " + postingList.size());
				
				for (Integer docID : postingList.keySet())
				{
					doc_to_score.add(docID);
				}
				
			}
			 else {
				 //System.out.println("Token no in indexer : " + querkToken);
				 continue;
			}
		}
		
		HashMap<Integer,Double> score_map = new HashMap<Integer,Double>();
		
		for (Integer docID : doc_to_score)
		{
			
			double score =scorer.rsv(indexer.getIndex(), qmap, docID, field);
				score_map.put(docID,score);
		}
		//System.out.println("-----");
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

		// TOP 10

		int j=0;
		int i=0;
		while (i<result.size() && j<10){
			Document doc;
			doc = index.getDocument(result.get(j).docID1);
			i++;
			//System.out.println( doc.getField("score"));
			if (Integer.parseInt(doc.getField("score"))< scoreLimit)
			{
				continue;
			}
			double doc_time = Double.parseDouble(doc.getField("created_utc"));
			if (Math.abs(( doc_time- ( System.currentTimeMillis() / 1000l))) > timeLimit)
			{
				System.out.println("skip");
				continue;
			}

			Date currentDate = new Date((long)doc_time*1000 - 28800000);
			DateFormat df = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss ");
				
			System.out.println(",");
			System.out.println( doc.getField("title"));
			System.out.println(",");
			System.out.println(df.format(currentDate));
			System.out.println(",");
			System.out.println( doc.getField("permalink"));
			System.out.println(",");
			System.out.println(doc.getField("content").substring(0,100));
			System.out.println(",");
			j++;
			
		}
		System.out.println("Time use:"+ String.format("%.02f",(System.nanoTime() - oldtime)*Math.pow(10,-6)) + "ms");
		System.out.println("--DONE--");
	}
	

}



