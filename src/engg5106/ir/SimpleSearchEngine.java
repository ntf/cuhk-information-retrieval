package engg5106.ir;

import java.io.*;
import java.util.*;
import java.io.File;

import engg5106.ir.crawlers.FileCrawler;
import engg5106.ir.crawlers.ICrawler;
import engg5106.ir.indexer.IIndexer;
import engg5106.ir.indexer.SimpleIndexer;

/**
 * @author NTF
 *
 */
public class SimpleSearchEngine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Hashtable<Integer, String> reverseMapping = new Hashtable<Integer, String>();
		Hashtable<String, Integer> dictionary = new Hashtable<String, Integer>();
		// read dictionary
		try {
			SimpleFileReader reader = new SimpleFileReader(new BufferedReader(
					new FileReader("data/dictionary.txt")));
			while (true) {
				String temp = reader.next();
				if (temp == null) {
					break;
				}

				int id = Integer.parseInt(temp);
				String word = reader.next();
				System.out.println(id + " : " + word);
				dictionary.put(word, id);
				reverseMapping.put(id, word);
			}

			SimpleSearchEngine engine = new SimpleSearchEngine(dictionary,
					reverseMapping);

			engine.setCrawler(new FileCrawler(new File("data/documents/")
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".txt");
						}
					})));
			engine.setIndexer(new SimpleIndexer());

			Dictionary<String, Long> crawlerData = new Hashtable<String, Long>();
			engine.start(crawlerData);
			
			//sample#
		/*	SimpleSearchEngine.questionA(engine, "youth");
			SimpleSearchEngine.questionB(engine, "look", "you", true);
			SimpleSearchEngine.questionC(engine, "look", "you", 5);
		*/	// Question 5 answer
			SimpleSearchEngine.questionA(engine, "yellow");
			SimpleSearchEngine.questionB(engine, "yellow", "year", false);
			SimpleSearchEngine.questionC(engine, "break", "day", 6);
			System.out.println("END");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static public void questionA(SimpleSearchEngine engine, String dumpKey)
			throws IOException {
		PrintWriter out = new PrintWriter("data/a.txt");
		out.println("word:" + dumpKey);
		LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> index = engine
				.getIndexer().getIndex();
		LinkedHashMap<Integer, ArrayList<Integer>> postingLists = index
				.get(engine.dictionary.get(dumpKey));
		for (int i = 1; i <= 191; i++) {
			ArrayList<Integer> positions = postingLists.get(i);
			if (positions != null) {
				StringBuilder sb = new StringBuilder();
				for (Integer pos : positions) {
					sb.append(" ");
					sb.append(pos);
				}
				out.println(i + "," + positions.size() + ":" + sb.toString());
			}
		}
		out.close();
	};

	static public void questionB(SimpleSearchEngine engine, String w1,
			String w2, boolean AndRelation) throws IOException {
		PrintWriter out = new PrintWriter("data/b.txt");
		// out.println("word:" + dumpKey);
		LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> index = engine
				.getIndexer().getIndex();
		LinkedHashMap<Integer, ArrayList<Integer>> list1 = index
				.get(engine.dictionary.get(w1));
		LinkedHashMap<Integer, ArrayList<Integer>> list2 = index
				.get(engine.dictionary.get(w2));

		Set<Integer> documents = new HashSet<Integer>(list1.keySet());

		if (AndRelation) {
			// intersection
			documents.retainAll(list2.keySet());
		} else {
			// union
			documents.addAll(list2.keySet());
		}

		ArrayList<Integer> docIds = new ArrayList<Integer>(documents);
		Collections.sort(docIds);
		for (Integer docId : docIds) {
			out.println(docId);
		}
		out.close();
	};

	static public void questionC(SimpleSearchEngine engine, String w1,
			String w2, Integer k) throws IOException {
		PrintWriter out = new PrintWriter("data/c.txt");

		// out.println("word:" + dumpKey);
		LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> index = engine
				.getIndexer().getIndex();
		LinkedHashMap<Integer, ArrayList<Integer>> list1 = index
				.get(engine.dictionary.get(w1));
		LinkedHashMap<Integer, ArrayList<Integer>> list2 = index
				.get(engine.dictionary.get(w2));

		Set<Integer> documents = list1.keySet();
		documents.retainAll(list2.keySet());

		ArrayList<Integer> docIds = new ArrayList<Integer>(documents);
		Collections.sort(docIds);

		for (Integer docId : docIds) {
			ArrayList<Integer> positions1 = list1.get(docId);

			ArrayList<Integer> positions2 = list2.get(docId);

			int size1 = positions1.size(), size2 = positions2.size();
			System.out.println("checking doc : " + docId);
			for (int i = 0; i < size1; i++) {
				for (int j = 0; j < size2; j++) {
					if (Math.abs(positions1.get(i) - positions2.get(j)) <= k) {
						out.println(docId + " " + positions1.get(i) + " "
								+ positions2.get(j));
					}

				}
			}

			/*
			 * int i = 0, j = 0; while (i < size1 && j < size2) { if
			 * (Math.abs(positions1.get(i) - positions2.get(j)) <= k) {
			 * out.println(docId + " " + positions1.get(i) + " " +
			 * positions2.get(j)); i++; j++; } else { if (positions1.get(i) <
			 * positions2.get(j)) { i++; } else { j++; } } }
			 */
		}
		out.close();
	};

	protected ICrawler crawler;
	protected IIndexer indexer;
	protected Dictionary<String, Integer> dictionary;
	protected Hashtable<Integer, String> reverseDictionary;

	public SimpleSearchEngine(Dictionary<String, Integer> dict,
			Hashtable<Integer, String> reverseDict) {

		this.dictionary = dict;
		this.reverseDictionary = reverseDict;
		System.out.println("Hello,World");
	}

	public ICrawler getCrawler() {
		return this.crawler;
	}

	public void setCrawler(ICrawler crawler) {
		this.crawler = crawler;
	}

	public IIndexer getIndexer() {
		return this.indexer;
	}

	public void setIndexer(IIndexer indexer) {
		indexer.setDictionary(this.dictionary);
		this.indexer = indexer;
	}

	public void start(Dictionary<String, Long> crawlerData) throws IOException {
		this.crawler.start(crawlerData, this.indexer);

	}
}
