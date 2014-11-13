package engg5106.ir.bm25;
import java.util.HashMap;

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


	public double rsv(TfIdfWeightedDocumentTermMatrix tfidf,HashMap<Integer, Integer> query) {
		double def_rsv = 0.0;
		for(int term : query.keySet()){
		
		int n =tfidf.sizeOfDocument();
		int df = tfidf.getDocFrequencies(term);
		
		
		}
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

	public HashMap<Integer, Integer> asQuery() {
		return this.map;
	}

	public int getTermCount() {
		return this.termCount;
	}
}
