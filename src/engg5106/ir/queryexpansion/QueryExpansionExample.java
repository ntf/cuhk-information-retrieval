package engg5106.ir.queryexpansion;

import java.util.*;

import engg5106.ir.QueryExpansion;



/**
 *
 * @author Sit
 */
public class QueryExpansionExample {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String query ="bus ";  // Query Entry
		int aa=0;
		for (aa=0;aa<args.length-6;aa++)
			query += " ";
		//System.out.println(query);
		
		//boolean qe = Boolean.valueOf(args[args.length-1]);
        QueryExpansion originalQuery = new QueryExpansion();
        // Query Expansion
    //    if (qe) {
            HashSet<String> expandedQuery = new HashSet<String>();

         //   System.out.println(originalQuery);
            expandedQuery = originalQuery.expand(query);
          //  query = "";
            for (String s : expandedQuery) {
                query += (s + " ");
                System.out.println(s);
            }
            System.out.println(expandedQuery);
     //   }
		
		
		/*
		String queryString = "bus";
		HashSet<String> expandedQuery = new HashSet<String>();

		QueryExpansion query = new QueryExpansion();
		expandedQuery = query.expand(queryString);
		for (String s : expandedQuery) {
			System.out.println(s);
		}*/
	}

}
