package engg5106.ir.queryexpansion;

import java.util.*;



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
		String queryString = "exercise";
		HashSet<String> expandedQuery = new HashSet<String>();

		QueryExpansion query = new QueryExpansion(queryString);
		expandedQuery = query.expand();
		for (String s : expandedQuery) {
			System.out.println(s);
		}
	}

}
