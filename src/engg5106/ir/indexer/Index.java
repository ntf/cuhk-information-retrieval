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
	protected transient NavigableSet<Fun.Tuple2<Integer, String>> documentDictionaryInverse;

	protected int termCount = 0;

	/**
	 * Map < Term (string) , termId >
	 */
	protected transient HTreeMap<String, Integer> termDictionary;
	/**
	 * ReverseMapping < termId , Term (string)
	 */
	protected transient NavigableSet<Fun.Tuple2<Integer, String>> termDictionaryInverse;

	/**
	 * HashMap< fieldName , MapDB< TermId , HashMap< DocId, DocumentFrequency>
	 * >>
	 */
	protected HashMap<String, HTreeMap<Integer, HashMap<Integer, Integer>>> index;

	private transient IndexOptions[] options;

	public transient DB db;
	


	public Index() {
		test = "test1";

	}

	public void initialize() {
		analyzer = new StandardAnalyzer();

		this.documents = this.db.createTreeMap("documents").counterEnable()
				.makeOrGet();

		this.termDictionary = this.db.createHashMap("termDictionary")
				.counterEnable().makeOrGet();
		// inverse mapping for primary map
		termDictionaryInverse = new TreeSet<Fun.Tuple2<Integer, String>>();
		// bind inverse mapping to primary map, so it is auto-updated
		Bind.mapInverse(this.termDictionary, termDictionaryInverse);

		this.documentDictionary = this.db.createHashMap("documentDictionary")
				.counterEnable().makeOrGet();
		documentDictionaryInverse = new TreeSet<Fun.Tuple2<Integer, String>>();
		Bind.mapInverse(this.documentDictionary, documentDictionaryInverse);

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

		this.documentCount = this.documents.size();
		this.termCount = this.termDictionary.size();
	}

	public void setDB(DB db) {
		this.db = db;
	}

	public void setOptions(IndexOptions[] options) {
		this.options = options;
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
			docId = this.documentDictionary.get(key);
		} else {
			this.documentDictionary.put(key, this.documentCount);
			docId = this.documentCount;
			this.documentCount++;
		}

		this.documents.put(key, doc);

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
			
			int doc_length = 0;
			HashMap<Integer, Integer> lavg;

			String value = doc.getField(option.getField());
			if (value != null) {
				if (option.getType() == IndexOptions.Type.Tokenize) {
					List<String> tokens = Index.tokenize(analyzer, value);
					doc_length = tokens.size();
					for (String token : tokens) {
						if (this.termDictionary.containsKey(token)) {
							termId = this.termDictionary.get(token);
						} else {
							this.termDictionary.put(token, this.termCount);
							termId = this.termCount;
							this.termCount++;
						}
						this.addDocumentToTerm(tierIndex, docId, termId);
						this.addDocumentToTerm(tierIndex, docId, 99999998); // Stores doc length
					}
					tokens.clear();

				} else if (option.getType() == IndexOptions.Type.Keyword) {
					if (this.termDictionary.containsKey(value)) {
						termId = this.termDictionary.get(value);
					} else {
						this.termDictionary.put(value, this.termCount);
						termId = this.termCount;
						this.termCount++;
					}
					this.addDocumentToTerm(tierIndex, docId, termId);
				}
			}
			
			if (!tierIndex.containsKey(99999999)){   // Use Term id 99999999, docid 0 's frequency used to store the total doc length
				lavg = new HashMap<Integer, Integer>();
			 	tierIndex.put(99999999, lavg);
			}
			else
				lavg = tierIndex.get(99999999);
			
			if (!lavg.containsKey(0)) 
				lavg.put(0,doc_length);
			else
			{
				lavg.put(0,doc_length + lavg.get(0));
			}
			tierIndex.put(99999999, lavg);

		}
	}

	public void addDocumentToTerm(
			HTreeMap<Integer, HashMap<Integer, Integer>> tierIndex, int docId,
			int termId) {
		HashMap<Integer, Integer> termIndex;
		if (!tierIndex.containsKey(termId)) {
			termIndex = new HashMap<Integer, Integer>();
			tierIndex.put(termId, termIndex);
		} else {
			termIndex = tierIndex.get(termId);
		}

		if (!termIndex.containsKey(docId)) {
			termIndex.put(docId, 1);
		} else {
			termIndex.put(docId, (termIndex.get(docId) + 1));
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
		for (String key : Fun.filter(this.documentDictionaryInverse, docId)) {
			Document doc = this.get(key);
			return doc;
		}

		return null;
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


	public int getAvgDocLength(String field) {
		int tid = 99999999;
		int did = 2;
		if (this.index.containsKey(field)) {
			HTreeMap<Integer, HashMap<Integer, Integer>> a = this.index
					.get(field);
			if (a.containsKey(tid)) {
				System.out.println("OK");
				HashMap<Integer, Integer> b = a.get(tid);
				System.out.println("Size" + " : " + a.size());
		        System.out.println("Size" + " : " + b.size());
		        
				if (b.containsKey(did)) {
					return b.get(did);
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
