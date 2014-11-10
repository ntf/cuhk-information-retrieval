/**
 * 
 */
package engg5106.ir.indexer;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author NTF
 *
 */
public class SimpleIndexer implements IIndexer {

	private Dictionary<String, Integer> dictionary;
	// word id , doc id , pos
	protected LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> index;

	protected int[] termFrequencies;
	protected int termCount;

	public SimpleIndexer() {
		// initialize the index
		this.index = new LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>>();

	}

	@Override
	public void setDictionary(Dictionary<String, Integer> dictionary) {
		this.dictionary = dictionary;
		this.termCount = 0;
		this.termFrequencies = new int[dictionary.size()];
	}

	@Override
	public void add(String word, Integer docId, int pos) {
		Integer wordId = dictionary.get(word);
		LinkedHashMap<Integer, ArrayList<Integer>> wordIndex = index
				.get(wordId);
		if (wordIndex == null) {
			wordIndex = new LinkedHashMap<Integer, ArrayList<Integer>>();
			index.put(wordId, wordIndex);
		}
		ArrayList<Integer> docIndex = wordIndex.get(docId);
		if (docIndex == null) {
			docIndex = new ArrayList<Integer>();
			wordIndex.put(docId, docIndex);
		}
		docIndex.add(pos);

		this.termFrequencies[wordId]++;
		this.termCount++;
	}

	public int getTermCount() {
		return this.termCount;
	}

	public int getTermCount(int termId) {
		return this.termFrequencies[termId];
	}

	public LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> getIndex() {
		return this.index;
	}
}
