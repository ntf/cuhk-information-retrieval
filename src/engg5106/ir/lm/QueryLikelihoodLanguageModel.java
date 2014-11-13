package engg5106.ir.lm;

import java.util.HashMap;

import engg5106.ir.smoothing.JelinekMercerSmoothing;
/**
 * 
 * @author NTF
 *@deprecated
 */
public class QueryLikelihoodLanguageModel {

	private HashMap<Integer, Integer> map;

	private int termCount;

	private JelinekMercerSmoothing smoother;

	public QueryLikelihoodLanguageModel(int terms, int docID) {
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

	public void setSmoother(JelinekMercerSmoothing smoother) {
		this.smoother = smoother;
	}

	public double P(int termID) {
		double t = this.map.containsKey(termID) ? (double) this.map.get(termID) / (double) this.termCount : 0.0;
		return this.smoother
				.apply(termID, this.map.containsKey(termID) ? t : 0);

	}

	public double P(HashMap<Integer, Integer> query) {
		double result = 1.0;
		for (int term : query.keySet()) {
			// 0.25 * 0.25 = 0.25^2
			result *= Math.pow(this.P(term), (double) query.get(term));
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
