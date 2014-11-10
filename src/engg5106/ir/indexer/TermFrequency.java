package engg5106.ir.indexer;

import java.io.Serializable;

public class TermFrequency implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int docId;
	public int termId;
	public int frequency;

	public TermFrequency(int d, int t, int f) {
		this.docId = d;
		this.termId = t;
		this.frequency = f;
	}

	public TermFrequency() {
	}
}
