package engg5106.ir;

import edu.smu.tspell.wordnet.*;

import java.util.*;

/**
 *
 * @author Sit
 */
public class QueryExpansion {

        private String query = null;

	public QueryExpansion() {

		QueryExpansion.setDictionaryPath("test/WordNet-3.0/dict/");
	}

	public static void setDictionaryPath(String path) {
		System.setProperty("wordnet.database.dir", path);
	}

	public HashSet<String> expand(String q) {
		QueryExpansion.setDictionaryPath("test/WordNet-3.0/dict/");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		HashSet<String> synonyms = new HashSet<String>();

		
		Synset[] synsets = database.getSynsets(q);
		for (int i = 0; i < synsets.length; i++) {
			String[] wordForms;
			wordForms = synsets[i].getWordForms();
			for (int j = 0; j < wordForms.length; j++) {
				synonyms.add(wordForms[j]);
			}
		}

		return synonyms;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public HashSet<String> expand(SynsetType type) {
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		HashSet<String> synonyms = new HashSet<String>();
		Synset[] synsets;

		if (type == SynsetType.ADJECTIVE) {
			synsets = database.getSynsets(this.query, SynsetType.ADJECTIVE);
		} else if (type == SynsetType.ADJECTIVE_SATELLITE) {
			synsets = database.getSynsets(this.query,
					SynsetType.ADJECTIVE_SATELLITE);
		} else if (type == SynsetType.ADVERB) {
			synsets = database.getSynsets(this.query, SynsetType.ADVERB);
		} else if (type == SynsetType.NOUN) {
			synsets = database.getSynsets(this.query, SynsetType.NOUN);
		} else if (type == SynsetType.VERB) {
			synsets = database.getSynsets(this.query, SynsetType.VERB);
		} else {
			throw new IllegalArgumentException(
					"No such word type! Only accept ADJECTIVE, ADJECTIVE_SATELLITE, ADVERB, NOUN, VERB");
		}

		for (int i = 0; i < synsets.length; i++) {
			String[] wordForms;
			wordForms = synsets[i].getWordForms();
			for (int j = 0; j < wordForms.length; j++) {
				synonyms.add(wordForms[j]);
			}
		}

		return synonyms;
	}
	

}
