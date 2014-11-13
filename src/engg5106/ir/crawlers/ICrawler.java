/**
 * 
 */
package engg5106.ir.crawlers;

import java.io.IOException;
import java.util.Dictionary;

import engg5106.ir.indexer.IIndexer;

/**
 * @author NTF
 * @deprecated
 */
public interface ICrawler {

	void start(Dictionary<String, Long> list, IIndexer indexer) throws IOException;

}
