/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Sit
 */
import edu.smu.tspell.wordnet.*;
import java.util.*;

public class Query {
    
    private String query = new String();
    
    Query(String s) {
        this.query = s;
    }
    
    public HashSet expand() {
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        HashSet<String> synonyms = new HashSet<String>();

        Synset[] synsets = database.getSynsets(this.query); 
        for (int i = 0; i < synsets.length; i++) { 
            String[] wordForms;
            wordForms = synsets[i].getWordForms();
            for (int j = 0; j < wordForms.length; j++) {
                synonyms.add(wordForms[j]);
            }
        }
        
        return synonyms;
    }
    
    // First argument is to specify the word type. Accept ADJECTIVE, ADJECTIVE_SATELLITE, ADVERB, NOUN and VERB as input.
    public HashSet expand(String s) {
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        HashSet<String> synonyms = new HashSet<String>();
        Synset[] synsets;

        if (s == "ADJECTIVE") {
            synsets = database.getSynsets(this.query, SynsetType.ADJECTIVE); 
        } else if (s == "ADJECTIVE_SATELLITE") {
            synsets = database.getSynsets(this.query, SynsetType.ADJECTIVE_SATELLITE);
        } else if (s == "ADVERB") {
            synsets = database.getSynsets(this.query, SynsetType.ADVERB);
        } else if (s == "NOUN") {
            synsets = database.getSynsets(this.query, SynsetType.NOUN);
        } else if (s == "VERB") {
            synsets = database.getSynsets(this.query, SynsetType.VERB);
        } else {
            throw new IllegalArgumentException("No such word type! Only accept ADJECTIVE, ADJECTIVE_SATELLITE, ADVERB, NOUN, VERB");
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
