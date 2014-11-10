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

import engg5106.ir.Document;
import gnu.trove.map.hash.THashMap;

public class Index implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String test;
	protected transient Analyzer analyzer;

	protected int documentCount = 0;
	protected Map<String, Document> documents;
	protected transient DualHashBidiMap<String, Integer> documentDictionary;

	protected int termCount = 0;
	protected transient DualHashBidiMap<String, Integer> termDictionary;

	/**
	 * fieldName , TermId , DocId, TermDocumentFrequency size of ArrayList =
	 * DocumentFrequency
	 */
	protected THashMap<String, THashMap<Integer, THashMap<Integer, Integer>>> index;

	private transient IndexOptions[] options;

	public Index() {
		test = "test1";
		documents = new THashMap<String, Document>();

		this.termDictionary = new DualHashBidiMap<String, Integer>();
		this.documentDictionary = new DualHashBidiMap<String, Integer>();

		this.index = new THashMap<String, THashMap<Integer, THashMap<Integer, Integer>>>();
		this.initialize();
	}

	public void initialize() {
		analyzer = new StandardAnalyzer();
	}

	public void setOptions(IndexOptions[] options) {
		this.options = options;
	}

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

		// this.documents.put(key, doc);

		// Index implementation
		for (IndexOptions option : this.options) {
			THashMap<Integer, THashMap<Integer, Integer>> tierIndex;
			if (!index.containsKey(option.getField())) {
				tierIndex = new THashMap<Integer, THashMap<Integer, Integer>>();
				index.put(option.getField(), tierIndex);
			} else {
				tierIndex = index.get(option.getField());
			}

			String value = doc.getField(option.getField());
			if (value != null) {
				if (option.getType() == IndexOptions.Type.Tokenize) {
					List<String> tokens = Index.tokenize(analyzer, value);
					for (String token : tokens) {
						if (this.termDictionary.containsKey(token)) {
							termId = this.termDictionary.get(token);
						} else {
							this.termDictionary.put(token, this.termCount);
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
						termId = this.termCount;
						this.termCount++;
					}
					this.addDocumentToTerm(tierIndex, docId, termId);
				}
			}
		}
	}

	public void addDocumentToTerm(
			THashMap<Integer, THashMap<Integer, Integer>> tierIndex,
			Document doc, String term) {
		int termId, docId;

		if (this.termDictionary.containsKey(term)) {
			termId = this.termDictionary.get(term);
		} else {
			this.termDictionary.put(term, this.termCount);
			termId = this.termCount;
			this.termCount++;
		}

		String key = doc.key();
		if (this.documents.containsKey(key)) {
			docId = this.documentDictionary.get(key);
		} else {
			this.documentDictionary.put(key, this.documentCount);
			docId = this.documentCount;
			this.documentCount++;
		}
		this.addDocumentToTerm(tierIndex, docId, termId);
	}

	public void addDocumentToTerm(
			THashMap<Integer, THashMap<Integer, Integer>> tierIndex, int docId,
			int termId) {

		THashMap<Integer, Integer> termIndex;

		if (!tierIndex.containsKey(termId)) {
			termIndex = new THashMap<Integer, Integer>();
			tierIndex.put(termId, termIndex);
		} else {
			termIndex = tierIndex.get(termId);
		}

		if (!termIndex.containsKey(docId)) {
			termIndex.put(docId, 1);
		} else {
			termIndex.put(docId, termIndex.get(docId) + 1);
		}
	}

	public Document get(String key) {
		if (this.documents.containsKey(key)) {
			return this.documents.get(key);
		} else {
			return null;
		}
	}

	public void listDocuments() {
		for (Entry<String, Document> entry : this.documents.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = "
					+ entry.getValue().getField("title"));
		}
	}

	public DualHashBidiMap<String, Integer> getDocumentDictionary() {
		return this.documentDictionary;
	}

	public void setDocumentDictionary(DualHashBidiMap<String, Integer> d) {
		this.documentDictionary = d;
	}

	public DualHashBidiMap<String, Integer> getTermDictionary() {
		return this.termDictionary;
	}

	public void setTermDictionary(DualHashBidiMap<String, Integer> d) {
		this.termDictionary = d;
	}

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
}
