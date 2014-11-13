package engg5106.ir.bm25;
import java.util.HashMap;
import java.util.StringTokenizer;

import engg5106.ir.matrix.TfIdfWeightedDocumentTermMatrix;


public class bm25 {
	private HashMap<Integer, Integer> map;

	private int termCount;

	public bm25(int terms, int docID) {
		this.termCount = 0;
		this.map = new HashMap<Integer, Integer>();
	}

	public void add(int term, int count) {
		if (!this.map.containsKey(term)) {
			this.map.put(term, 0);
		}

		this.map.put(term, this.map.get(term) + count);
		this.termCount += count;
	}


	public double rsv(TfIdfWeightedDocumentTermMatrix tfidf,String query, int doc) {
		double def_rsv = 0.0;
		double k1=1.5;
		double k3=1.5;
		double b =0.75;
		int n =tfidf.sizeOfDocument();
		int ld = tfidf.getDocLenght(doc); // Length of the current doc
		double lave =  tfidf.getAvgDocLenght() ; // Length of the average doc
		
        StringTokenizer tokens = new StringTokenizer(query);
      /*  while(tokens.hasMoreTokens()) {

        	tokens.nextToken()    // Take back the term id

			int df = tfidf.getDocFrequencies(term);
			def_rsv += Math.log10(n/df) * (((k1+1)*tfidf.getFrequencies(term, doc)) / (k1*((1-b)+b(ld/lave)+tfidf.getFrequencies(term, doc))))* (((k3+1)*tfq) / (k3 + tfq));
	
		
		}*/
		return def_rsv;
	}
	
	public double P(HashMap<Integer, Integer> query) {
		double result = 1.0;
		for (int term : query.keySet()) {
			// 0.25 * 0.25 = 0.25^2
			//result *= Math.pow(this.P(term), (double) query.get(term));
		}
		return result;
	}


	public int getTermCount() {
		return this.termCount;
	}
}
