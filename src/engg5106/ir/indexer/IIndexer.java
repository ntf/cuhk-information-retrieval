/**
 * 
 */
package engg5106.ir.indexer;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedHashMap;

/**
 * @author NTF
 *
 */
public interface IIndexer {

	void setDictionary(Dictionary<String, Integer> dictionary);

	void add(String word, Integer docId, int pos);

	/**
	 * term count in the collection
	 * @return
	 */
	int getTermCount();

	/**
	 * term frequencies count in the collection
	 * @param termId
	 * @return
	 */
	int getTermCount(int termId);

	LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> getIndex();
}
