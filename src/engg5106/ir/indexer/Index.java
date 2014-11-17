package engg5106.ir.indexer;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.mapdb.Atomic.Var;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;

import engg5106.ir.Document;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Index implements Serializable {

	private static final long serialVersionUID = 1L;

	public String test;
	protected transient Analyzer analyzer;

	protected int documentCount = 0;

	/**
	 * Fast document key to Document storage Map <document-key , Document object
	 * >
	 */
	protected BTreeMap<String, Document> documents;
	/**
	 * Map< document-key , document-id >
	 */
	protected transient HTreeMap<String, Integer> documentDictionary;

	/**
	 * ReverseMapping < document-id , document-key>
	 */
	protected transient HTreeMap<Integer, String> documentDictionaryInverse;

	protected int termCount = 0;

	/**
	 * Map < Term (string) , termId >
	 */
	protected transient HTreeMap<String, Integer> termDictionary;
	/**
	 * ReverseMapping < termId , Term (string)
	 */
	protected transient HTreeMap<Integer, String> termDictionaryInverse;

	/**
	 * HashMap< fieldName , MapDB< TermId , HashMap< DocId, DocumentFrequency>
	 * >>
	 */
	protected HashMap<String, HTreeMap<Integer, HashMap<Integer, Integer>>> index;

	HTreeMap<String, Double> docLengthLogAverage;
	private transient IndexOptions[] options;

	public transient DB db;
	public transient DB db2;
	public Index() {
		test = "test1";

	}

	public void initialize() {
		analyzer = new StandardAnalyzer();

		this.documents = this.db2.createTreeMap("documents").counterEnable()
				.makeOrGet();

		this.termDictionary = this.db2.createHashMap("termDictionary")
				.counterEnable().makeOrGet();

		// inverse mapping for primary map
		termDictionaryInverse = this.db2.createHashMap("termDictionary-reverse")
				.counterEnable().makeOrGet();

		this.documentDictionary = this.db2.createHashMap("documentDictionary")
				.counterEnable().makeOrGet();
		documentDictionaryInverse = this.db2
				.createHashMap("documentDictionary-reverse").counterEnable()
				.makeOrGet();

		this.index = new HashMap<String, HTreeMap<Integer, HashMap<Integer, Integer>>>();

		for (IndexOptions option : this.options) {
			HTreeMap<Integer, HashMap<Integer, Integer>> tierIndex;
			if (!index.containsKey(option.getField())) {
				tierIndex = this.db.createHashMap("index-" + option.getField())
						.counterEnable().makeOrGet();
				index.put(option.getField(), tierIndex);
			} else {
				tierIndex = index.get(option.getField());
			}
		}

		docLengthLogAverage = this.db.createHashMap(
				"average-document-length-by-fields").makeOrGet();
		this.documentCount = this.documents.size();
		this.termCount = this.termDictionary.size();
	}

	/**
	 * Geometric mean we store the sum of the log value of document length
	 * 
	 * @param length
	 */
	protected void addDocumentLength(String field, int length) {
		double d = 0.0;
		if (length > 0) {
			if (this.docLengthLogAverage.containsKey(field)) {
				d = docLengthLogAverage.get(field);
			}
			if (d + Math.log10(length) == Double.NEGATIVE_INFINITY) {
				System.out.println("debug");
			}
			docLengthLogAverage.put(field, d + Math.log10(length));
		}
	}

	/**
	 * Geometric mean via log-average
	 * 
	 * This is sometimes called the log-average (not to be confused with the
	 * logarithmic average). It is simply computing the arithmetic mean of the
	 * logarithm-transformed values of a_i (i.e., the arithmetic mean on the log
	 * scale) and then using the exponentiation to return the computation to the
	 * original scale
	 * 
	 * @return
	 */
	protected double getAverageDocumentLength(String field) {
		double d = docLengthLogAverage.get(field);
		return Math.pow(10, d / (double) this.documents.size());
	}

	public void setDB(DB db,DB db2) {
		this.db = db;
		this.db2 = db2;
	}

	public void setOptions(IndexOptions[] options) {
		this.options = options;
	}

	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	/**
	 * add document to the index
	 * 
	 * @param doc
	 */
	public void add(Document doc) {
		String key = doc.key();

		int docId, termId;

		if (this.documents.containsKey(key)) {
			// docId = this.documentDictionary.get(key);
			// skip
			return;
		} else {
			this.documentDictionary.put(key, this.documentCount);
			this.documentDictionaryInverse.put(this.documentCount, key);
			docId = this.documentCount;
			this.documentCount++;
		}

		// Index implementation
		for (IndexOptions option : this.options) {
			HTreeMap<Integer, HashMap<Integer, Integer>> tierIndex;
			if (!index.containsKey(option.getField())) {
				tierIndex = this.db.createHashMap("index-" + option.getField())
						.counterEnable().makeOrGet();
				index.put(option.getField(), tierIndex);
			} else {
				tierIndex = index.get(option.getField());
			}

			String value = doc.getField(option.getField());
			if (value != null) {

				List<String> tokens = Index.tokenize(analyzer, value);
				this.addDocumentLength(option.getField(), tokens.size());

				if (option.getType() == IndexOptions.Type.Tokenize) {
					for (String token : tokens) {
						if (this.termDictionary.containsKey(token)) {
							termId = this.termDictionary.get(token);
						} else {
							this.termDictionary.put(token, this.termCount);
							this.termDictionaryInverse.put(this.termCount,
									token);
							termId = this.termCount;
							this.termCount++;
						}
						this.addDocumentToTerm(tierIndex, docId, termId);
					}
					tokens.clear();

				} else if (option.getType() == IndexOptions.Type.Keyword) {
					if (this.termDictionary.containsKey(value)) {
						termId = this.termDictionary.get(value);
					} else {
						this.termDictionary.put(value, this.termCount);
						this.termDictionaryInverse.put(this.termCount, value);
						termId = this.termCount;
						this.termCount++;
					}
					this.addDocumentToTerm(tierIndex, docId, termId);
				}
			}

		}
		this.documents.put(key, doc);
		System.out.println("added " + doc.getField("permalink"));
		if(this.documentCount % 50 == 0){
			this.db.commit();
		}
	}

	public void addDocumentToTerm(
			HTreeMap<Integer, HashMap<Integer, Integer>> tierIndex, int docId,
			int termId) {
		try {
			HashMap<Integer, Integer> termIndex;
			termIndex = tierIndex.get(termId);
			if (termIndex == null) {
				termIndex = new HashMap<Integer, Integer>();
				tierIndex.put(termId, termIndex);
			}/*
			 * else { termIndex = tierIndex.get(termId); }
			 */

			if (!termIndex.containsKey(docId)) {
				termIndex.put(docId, 1);
			} else {
				termIndex.put(docId, (termIndex.get(docId) + 1));
			}

			// don't forget to save the term index
			tierIndex.put(termId, termIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get document by document-key
	 * 
	 * @param key
	 * @return
	 */
	public Document get(String key) {
		if (this.documents.containsKey(key)) {
			return this.documents.get(key);
		} else {
			return null;
		}
	}

	/**
	 * get document by document-id
	 * 
	 * @param docId
	 * @return
	 */
	public Document getDocument(int docId) {
		if (this.documentDictionaryInverse.containsKey(docId)) {
			return this.documents
					.get(this.documentDictionaryInverse.get(docId));
		} else {
			return null;
		}
	}

	/**
	 * get document-id by document
	 * 
	 * @param doc
	 * @return
	 */
	public int getDocumentId(Document doc) {
		String key = doc.key();
		if (this.documents.containsKey(key)) {
			return this.documentDictionary.get(key);
		}

		return -1;
	}

	/**
	 * get termId by term
	 * 
	 * @param term
	 * @return
	 */
	public int getTermId(String term) {
		if (this.termDictionary.containsKey(term)) {
			return this.termDictionary.get(term);
		}

		return -1;
	}

	/**
	 * get term frequency by field (index-name) , termId , docId
	 * 
	 * @param field
	 * @param termId
	 * @param docId
	 * @return
	 */
	public int getTermFrequency(String field, int termId, int docId) {
		if (this.index.containsKey(field)) {
			HTreeMap<Integer, HashMap<Integer, Integer>> a = this.index
					.get(field);
			if (a.containsKey(termId)) {
				HashMap<Integer, Integer> b = a.get(termId);
				if (b.containsKey(docId)) {
					return b.get(docId);
				}
			}
		}
		return 0;
	}

	/**
	 * get document frequency by field (index-name) , termId
	 * 
	 * @param field
	 * @param termId
	 * @return
	 */
	public int getDocumentFrequency(String field, int termId) {
		if (this.index.containsKey(field)) {
			HTreeMap<Integer, HashMap<Integer, Integer>> a = this.index
					.get(field);
			if (a.containsKey(termId)) {
				HashMap<Integer, Integer> b = a.get(termId);
				// expensive
				return b.size();
			}
		}
		return 0;
	}

	public HashMap<Integer, Integer> getPositingList(String field, int termId) {
		if (this.index.containsKey(field)) {
			HTreeMap<Integer, HashMap<Integer, Integer>> a = this.index
					.get(field);
			if (a.containsKey(termId)) {
				HashMap<Integer, Integer> b = a.get(termId);
				return b;
			}
		}
		return null;
	}

	/**
	 * list documents debug use
	 */
	public void listDocuments() {
		for (Entry<String, Document> entry : this.documents.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = "
					+ entry.getValue().getField("title"));
		}
	}

	public HTreeMap<String, Integer> getDocumentDictionary() {
		return this.documentDictionary;
	}

	public void setDocumentDictionary(HTreeMap<String, Integer> d) {
		this.documentDictionary = d;
	}

	public HTreeMap<String, Integer> getTermDictionary() {
		return this.termDictionary;
	}

	public void setTermDictionary(HTreeMap<String, Integer> d) {
		this.termDictionary = d;
	}

	/**
	 * tokenizer
	 * 
	 * @param analyzer
	 * @param string
	 * @return
	 */
	public static List<String> tokenize(Analyzer analyzer, String string) {
		List<String> result = new ArrayList<String>();
		try {
			TokenStream stream = analyzer.tokenStream(null, new StringReader(
					string));
			stream.reset();
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class)
						.toString());
			}
			stream.close();
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		return result;
	}

	public int getDocumentCount() {
		return this.documentCount;
	}

	public void debug() {
		System.out.println("debug");

		for (Entry<String, HTreeMap<Integer, HashMap<Integer, Integer>>> tier : this.index
				.entrySet()) {
			System.out.println("Tier " + tier.getKey() + " has size = "
					+ tier.getValue().size());
		}
		System.out.println("Total number of documents = "
				+ this.documents.size());

	}
}
