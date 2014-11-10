package engg5106.ir.smoothing;

import engg5106.ir.indexer.IIndexer;

public class JelinekMercerSmoothing {

	private IIndexer collection;
	private double coefficient;

	public JelinekMercerSmoothing(IIndexer indexer, double coefficient) {
		this.collection = indexer;
		this.coefficient = coefficient;
	}

	public double apply(int termId, double originalValue) {
		double first = this.coefficient * originalValue;
		double second = (1.0 - this.coefficient)
				* ((double) this.collection.getTermCount(termId) / (double) this.collection
						.getTermCount());

		return first + second;
	}

}
