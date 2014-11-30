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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;





import engg5106.ir.bm25.bm25;
import engg5106.ir.indexer.Index;
import engg5106.ir.indexer.IndexOptions;
import engg5106.ir.queryexpansion.QueryExpansion;

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
		;
		String query ="";  // Query Entry
		int aa=0;
		for (aa=0;aa<args.length-6;aa++)
			query += (args[aa]+" ");
		//System.out.println(query);
		String field = "title"; 
		int result_length = Integer.parseInt(args[args.length-6]);
		boolean searchComment = Boolean.valueOf(args[args.length-5]);
		int scoreLimit = Integer.parseInt(args[args.length-4]);
		int timeLimit = Integer.parseInt(args[args.length-3]);
		boolean andor = Boolean.valueOf(args[args.length-2]);
		boolean qe = Boolean.valueOf(args[args.length-1]);
		//August 15-20 of August 2013
		

        // Query Expansion
        if (qe) {
        	andor= false;
        	StringTokenizer st1= new StringTokenizer(query);
			while (st1.hasMoreTokens()){
	            HashSet<String> expandedQuery = new HashSet<String>();

	            QueryExpansion originalQuery = new QueryExpansion(st1.nextToken());
	            expandedQuery = originalQuery.expand();
	            query = "";
	            for (String s : expandedQuery) {
	                query += (s + " ");
	                
	            }
	            //System.out.println(expandedQuery);
			}
        	

           
        }
        List<String> tokens = index.tokenize(index.getAnalyzer(), query); // Normalized query
        /*
         * 

		String queryString = "bus";
		HashSet<String> expandedQuery = new HashSet<String>();

		QueryExpansion query = new QueryExpansion(queryString);
		expandedQuery = query.expand();
		for (String s : expandedQuery) {
			System.out.println(s);
		}
	}
         */
        
        System.out.println();
        
        
         
        
		int q_termid;
		int firstdoc=0;
		Set<Integer> doc_to_score = new TreeSet<>();
		Set<Integer> temp_doc2 = new TreeSet<>();
		HashMap<Integer, Integer> qmap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> postingList = new HashMap<Integer, Integer>();
		long oldtime = 0;
		oldtime = System.nanoTime();
		
		
		for (String querkToken : tokens) {
			//System.out.println("zz");
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
				//System.out.println(postingList);
				for (Integer docID : postingList.keySet())
				{
					
						if(firstdoc==0)
						{
							doc_to_score.add(docID);
							
						}
						else if(andor)
						{
							temp_doc2.add(docID);
							
						}
						else
						{
							doc_to_score.add(docID);
						}
				}
				
				if (andor && firstdoc ==1)
				doc_to_score.retainAll(temp_doc2); // AND
				
				firstdoc =1;
				
			}
			 else {
				 //System.out.println("Token no in indexer : " + querkToken);
				 continue;
			}
		}
		
		
		//System.out.println(doc_to_score);
		
		HashMap<Integer,Double> score_map = new HashMap<Integer,Double>();
		
		for (Integer docID : doc_to_score)
		{
			double score =scorer.rsv(indexer.getIndex(), qmap, docID, field);
				score_map.put(docID,score);
		}
		
		//System.out.println("-----");
		if (searchComment){ // Need to search with comment
			doc_to_score.clear();
			temp_doc2.clear();
			field = "content";
			firstdoc=0;
			
			for (String querkToken : tokens) {
				//System.out.println("zz");
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
					//System.out.println(postingList);
					for (Integer docID : postingList.keySet())
					{
						
							if(firstdoc==0)
							{
								doc_to_score.add(docID);
							}
							else if(andor)
							{
								temp_doc2.add(docID);
								}
							else
							{
								doc_to_score.add(docID);
							}
					}
					
					if (andor && firstdoc ==1)
					doc_to_score.retainAll(temp_doc2); // AND
					
					firstdoc =1;
					
				}
				 else {
					 //System.out.println("Token no in indexer : " + querkToken);
					 continue;
				}
			}
			
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
		System.out.println(String.format("%.02f",(System.nanoTime() - oldtime)*Math.pow(10,-6)) + "ms");

		
		
		int j=0;
		int i=0;
		int k=0;
		
		while (i<result.size()){
			Document doc;
			doc = index.getDocument(result.get(i).docID1);
			i++;
			//System.out.println( doc.getField("score"));
			if (Integer.parseInt(doc.getField("score"))< scoreLimit)
			{
				continue;
			}
			double doc_time = Double.parseDouble(doc.getField("created_utc"));
			//System.out.println(Math.abs((  ( System.currentTimeMillis() / 1000l))- doc_time)+ "  "+ doc_time);
			if (Math.abs((  ( System.currentTimeMillis() / 1000l))- doc_time) > timeLimit)
			{
				//System.out.println("skip");
				continue;
			}
			k++;
			if (j<result_length)
			{
			Date currentDate = new Date((long)doc_time*1000 - 28800000);
			DateFormat df = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss ");
				
			System.out.println(",");
			System.out.println( doc.getField("title"));
			System.out.println(",");
			System.out.print(df.format(currentDate)); System.out.print("   Number of comment: "+ doc.getField("num_comments")); System.out.println("   Post score: "+ doc.getField("score"));
			System.out.println(",");
			System.out.println( doc.getField("permalink"));
			System.out.println(",");
			
			
			
			
			StringTokenizer st1= new StringTokenizer(doc.getField("content"));

			int abs =0;
			List<Integer> keyword_pos = new ArrayList<Integer>();
			while (st1.hasMoreTokens())
			{
				
				String ss = st1.nextToken();
				StringTokenizer st2= new StringTokenizer(query);
				
				while (st2.hasMoreTokens())
				{

					String qss = st2.nextToken();
					if (qss.equals(ss))
					{
						keyword_pos.add(abs);
					}
					
				}
				abs++;
			}
			

			List<String> content = new ArrayList<String>();
			
			st1= new StringTokenizer(doc.getField("content"));
			while (st1.hasMoreTokens()){
				content.add(st1.nextToken());
			}
			
			for (int pos : keyword_pos)
			{
				
				
				int nowpos= pos - 5;
				int count=0;
				if (pos>5)
				{
					System.out.print("...");
				}
				
				while (count<10)
				{
					if (nowpos>=0 && nowpos<content.size())
					{
						System.out.print(content.get(nowpos)+" ");
					}

					
				count++;
				nowpos++;
				}
				
				if (pos<content.size()-5)
				{
					System.out.print("...");
				}
				System.out.println("");
			}
			if (keyword_pos.size()==0)
			{
				int count=0;
				while (count<35 && count<content.size())
				{
				System.out.print(content.get(count)+" ");
				count++;
				}
				System.out.println("");
			}
			System.out.println(",");

			j++;
			}
		}
		System.out.println(k);
		//System.out.println("--DONE--");
	}
	

}



