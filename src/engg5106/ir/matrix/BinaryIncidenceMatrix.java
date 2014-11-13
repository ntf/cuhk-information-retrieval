package engg5106.ir.matrix;

/**
 * 
 * @author NTF
 * @deprecated
 */
public class BinaryIncidenceMatrix implements IMatrix {

	protected int termCount;
	protected int documentCount;

	protected boolean[][] index;

	public BinaryIncidenceMatrix(int termCount, int documentCount) {
		this.termCount = termCount;
		this.documentCount = documentCount;
		this.index = new boolean[termCount + 1][documentCount + 1];
	}

	public int sizeOfTerm() {
		return this.termCount;
	}

	public int sizeOfDocument() {
		return this.documentCount;
	}

	public boolean[][] getIndex() {
		return this.index;
	}

	public boolean get(int term, int doc) {
		return this.index[term][doc];
	}

	public int getInt(int term, int doc) {
		return this.get(term, doc) == true ? 1 : 0;
	}

	public void add(int term, int doc) {
		if (term <= 0 && doc <= 0 && term > termCount && doc > documentCount) {
			throw new IllegalArgumentException("");
		}
		this.index[term][doc] = true;
	}
}
