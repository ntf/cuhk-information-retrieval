package engg5106.ir.matrix;

public class TfIdfWeightedDocumentTermMatrix implements IMatrix {
	protected int termCount;
	protected int documentCount;

	protected double[][] index;
	public double[][] reverseIndex;
	protected int[][] tf;
	protected int[] df;

	public TfIdfWeightedDocumentTermMatrix(int termCount, int documentCount) {
		this.termCount = termCount;
		this.documentCount = documentCount;
		this.index = new double[termCount + 1][documentCount + 1];
		this.reverseIndex = new double[documentCount + 1][termCount + 1];
		this.tf = new int[termCount + 1][documentCount + 1];
		this.df = new int[termCount + 1];
	}

	public int sizeOfTerm() {
		return this.termCount;
	}

	public int sizeOfDocument() {
		return this.documentCount;
	}

	public double[][] getIndex() {
		return this.index;
	}

	public double get(int term, int doc) {
		return this.index[term][doc];
	}

	public int getFrequencies(int term, int doc) {
		return this.tf[term][doc];
	}

	public void add(int term, int doc) {
		if (term <= 0 && doc <= 0 && term > termCount && doc > documentCount) {
			throw new IllegalArgumentException("");
		}
		this.tf[term][doc]++;
	}

	public void add(int term, int doc, int count) {
		if (term <= 0 && doc <= 0 && term > termCount && doc > documentCount) {
			throw new IllegalArgumentException("");
		}
		this.tf[term][doc] += count;
		this.reverseIndex[doc][term] += count;
	}

	/**
	 * increment document frequency
	 * 
	 * @param term
	 */
	public void incrementDF(int term) {
		this.df[term]++;
	}

	/**
	 * Compute the tf-idf weighted index!
	 */
	public void compute() {
		int N = this.sizeOfDocument();

		for (int t = 1; t <= this.sizeOfTerm(); t++) {
			for (int d = 1; d <= this.sizeOfDocument(); d++) {
				double weight = this.tf[t][d] > 0 ? 1.0 + Math.log10(this.tf[t][d]) : 0;
				
				this.index[t][d] = (double) weight * Math.log10((double) N / this.df[t]);
			}
		}

	}
}
