WordNet is the dictionary necessary for QueryExpansion.
It should be placed under the directory “test/WordNet-3.0/dict/“.

Java API for WordNet Searching (JAWS) is used as the API
to retrieve data from WordNet. The corresponding jar file
“jaws-bin.jar” should be included in the Java library.

An example use:

public static void main(String[] args) {
	String query ="bus";
	QueryExpansion originalQuery = new QueryExpansion();
	HashSet<String> expandedQuery = new HashSet<String>();
	expandedQuery = originalQuery.expand(query);

	for (String s : expandedQuery) {
		query += (s + " ");
	}
}