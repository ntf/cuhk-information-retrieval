/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Sit
 */
import java.util.*;

public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String queryString = "exercise";
        HashSet<String> expandedQuery = new HashSet<String>();
        
        Query query = new Query(queryString);
        expandedQuery = query.expand("VERB");
        
        for (String s: expandedQuery) {
            System.out.println(s);
        }
    }
    
}
