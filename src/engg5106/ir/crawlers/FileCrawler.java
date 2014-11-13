/**
 * 
 */
package engg5106.ir.crawlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Dictionary;

import engg5106.ir.SimpleFileReader;
import engg5106.ir.indexer.IIndexer;

/**
 * @author NTF
 * @deprecated
 */
public class FileCrawler implements ICrawler {

	private File[] files;

	public FileCrawler(File[] listFiles) {
		this.files = listFiles;
		// TODO Auto-generated constructor stub
	}

	public void start(Dictionary<String, Long> histories, IIndexer indexer)
			throws IOException {

		for (File file : files) {
			Long lastModified = histories.get(file.getAbsolutePath());
			if (lastModified == null || lastModified < file.lastModified()) {

				// get the document ID
				Integer docId = Integer
						.parseInt(file.getName().split("[.]", 2)[0]);
				System.out
						.println("processing " + docId + " " + file.getName());

				SimpleFileReader reader = new SimpleFileReader(
						new BufferedReader(new FileReader(file)));

				int pos = 0;
				while (true) {
					String word = reader.next();

					if (word != null) {
						indexer.add(word, docId, pos);
						pos++;
					} else {
						break;
					}

				}
			}
		}
	}
}
