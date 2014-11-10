package engg5106.ir;

import java.io.*;
import java.util.*;
import java.io.File;

import engg5106.ir.crawlers.FileCrawler;
import engg5106.ir.crawlers.ICrawler;
import engg5106.ir.indexer.IIndexer;
import engg5106.ir.indexer.SimpleIndexer;
import engg5106.ir.lm.QueryLikelihoodLanguageModel;
import engg5106.ir.matrix.BinaryIncidenceMatrix;
import engg5106.ir.matrix.TfIdfWeightedDocumentTermMatrix;
import engg5106.ir.smoothing.JelinekMercerSmoothing;

/**
 * @author NTF
 *
 */
public class SimilarDocuments extends SimpleSearchEngine {

	static public class Similarity {
		public int docID1;
		public int docID2;
		public double value;

		public Similarity(int a, int b, double d) {
			this.docID1 = a;
			this.docID2 = b;
			this.value = d;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Similar Documents");
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

			{
				SimilarDocuments engine = new SimilarDocuments(dictionary,
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

				int[] leaders = new int[] { 1, 5, 40, 60 };

				BinaryIncidenceMatrix bi = new BinaryIncidenceMatrix(
						dictionary.size(), 191);

				TfIdfWeightedDocumentTermMatrix tfidf = new TfIdfWeightedDocumentTermMatrix(
						dictionary.size(), 191);

				LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> index = engine
						.getIndexer().getIndex();

				for (Integer termID : index.keySet()) {
					Iterable<Integer> docIDs = index.get(termID).keySet();
					for (Integer docID : docIDs) {
						bi.add(termID, docID);
						tfidf.add(termID, docID, index.get(termID).get(docID)
								.size());
						tfidf.incrementDF(termID);
					}
				}

				tfidf.compute();
				SimilarDocuments.vectorsDP = new double[tfidf.sizeOfDocument() + 1];
				SimilarDocuments.question4B(bi, leaders);

				SimilarDocuments.question4C(tfidf, leaders);
			}

			// Query-Likelihood Language model
			{
				System.out.println("Query-Likelihood Language model");
				SimilarDocuments engine = new SimilarDocuments(dictionary,
						reverseMapping);

				engine.setCrawler(new FileCrawler(new File(
						"data/documents-preprocessed/")
						.listFiles(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return name.toLowerCase().endsWith(".txt");
							}
						})));
				engine.setIndexer(new SimpleIndexer());

				Dictionary<String, Long> crawlerData = new Hashtable<String, Long>();
				engine.start(crawlerData);

				LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> index = engine
						.getIndexer().getIndex();

				QueryLikelihoodLanguageModel[] models = new QueryLikelihoodLanguageModel[191 + 1];

				// Jelinek-Mercer Smoothing and smooth coefficient
				JelinekMercerSmoothing smoother = new JelinekMercerSmoothing(
						engine.getIndexer(), 0.55);

				for (int i = 1; i <= 191; i++) {
					models[i] = new QueryLikelihoodLanguageModel(
							dictionary.size(), 191);
					models[i].setSmoother(smoother);
				}

				// build the Language Model
				for (Integer termID : index.keySet()) {
					Iterable<Integer> docIDs = index.get(termID).keySet();
					for (Integer docID : docIDs) {
						models[docID].add(termID, index.get(termID).get(docID)
								.size());
					}
				}

				// query
				PrintWriter out = new PrintWriter("data/e.txt");
				int queryId = 2;

				HashMap<Integer, Integer> query = models[queryId].asQuery();

				ArrayList<Similarity> result = new ArrayList<Similarity>();
				for (int i = 1; i <= 191; i++) {
					if (queryId != i) {
						double prob = models[i].P(query);
						result.add(new Similarity(queryId, i, prob));
					}
				}
				Collections.sort(result, new Comparator<Similarity>() {
					@Override
					public int compare(Similarity first, Similarity second) {

						return -1 * Double.compare(first.value, second.value);
					}
				});
				StringBuffer sb = new StringBuffer(queryId + ":");
				// TOP 10
				for (int j = 0; j < 10; j++) {
					if (j != 0) {
						sb.append(",");
					}
					sb.append(result.get(j).docID2);

					System.out.println(result.get(j).docID1 + " & "
							+ result.get(j).docID2 + " = "
							+ result.get(j).value);

				}
				out.println(sb.toString());
				out.close();
				// end scope
			}
			System.out.println("END");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static public float JaccardCoefficient(BinaryIncidenceMatrix bi,
			int docID1, int docID2) {
		int intersect = 0;
		int union = 0;
		for (int i = 0; i < bi.sizeOfTerm(); i++) {
			if (bi.get(i, docID1) == true && bi.get(i, docID2) == true) {
				intersect++;
				union++;
			} else if (bi.get(i, docID1) == true || bi.get(i, docID2) == true) {
				union++;
			}
		}
		return union != 0 ? (float) intersect / union : -1.0f;
	}

	static double[] vectorsDP;

	static public double vectorSize(TfIdfWeightedDocumentTermMatrix tfidf,
			int docID) {

		if (vectorsDP[docID] > 0) {
			return vectorsDP[docID];
		}

		double sum = 0;
		/*
		 * double j = 0; for (int i = 0; i < tfidf.sizeOfTerm(); i++) { if
		 * (tfidf.reverseIndex[docID][i] > 0) { j+=tfidf.reverseIndex[docID][i]
		 * ; } }
		 */

		// System.out.println("test :" +j);

		for (int i = 0; i < tfidf.sizeOfTerm(); i++) {
			sum += Math.pow(tfidf.get(i, docID), 2.0);
		}
		vectorsDP[docID] = Math.sqrt(sum);
		return vectorsDP[docID];
	}

	static public double dotProduct(TfIdfWeightedDocumentTermMatrix tfidf,
			int docID1, int docID2) {
		double sum = 0;
		for (int i = 0; i < tfidf.sizeOfTerm(); i++) {
			sum += tfidf.get(i, docID1) * tfidf.get(i, docID2);

		}
		return sum;
	}

	static public double CosineSimilarity(
			TfIdfWeightedDocumentTermMatrix tfidf, int docID1, int docID2) {

		return SimilarDocuments.dotProduct(tfidf, docID1, docID2)
				/ (SimilarDocuments.vectorSize(tfidf, docID1) * SimilarDocuments
						.vectorSize(tfidf, docID2));
	}

	static public void question4B(BinaryIncidenceMatrix bi, int[] leaders)
			throws IOException {
		PrintWriter out = new PrintWriter("data/b.txt");

		for (int leaderID : leaders) {
			ArrayList<Similarity> result = new ArrayList<Similarity>();
			for (int i = 1; i <= 191; i++) {
				if (leaderID != i) {

					result.add(new Similarity(leaderID, i, SimilarDocuments
							.JaccardCoefficient(bi, leaderID, i)));
				}
			}
			Collections.sort(result, new Comparator<Similarity>() {
				@Override
				public int compare(Similarity first, Similarity second) {

					return -1 * Double.compare(first.value, second.value);
				}
			});
			StringBuffer sb = new StringBuffer(leaderID + ":");
			// TOP 10
			for (int j = 0; j < 10; j++) {
				if (j != 0) {
					sb.append(",");
				}
				sb.append(result.get(j).docID2);
				/*
				 * System.out.println(result.get(j).docID1 + " & " +
				 * result.get(j).docID2 + " = " + result.get(j).value);
				 */
			}
			out.println(sb.toString());
		}

		out.close();
	}

	static public void question4C(TfIdfWeightedDocumentTermMatrix tfidf,
			int[] leaders) throws IOException {
		PrintWriter out = new PrintWriter("data/c.txt");

		for (int leaderID : leaders) {
			ArrayList<Similarity> result = new ArrayList<Similarity>();
			for (int i = 1; i <= 191; i++) {
				if (leaderID != i) {

					result.add(new Similarity(leaderID, i, SimilarDocuments
							.CosineSimilarity(tfidf, leaderID, i)));
				}
			}
			Collections.sort(result, new Comparator<Similarity>() {
				@Override
				public int compare(Similarity first, Similarity second) {

					return -1 * Double.compare(first.value, second.value);
				}
			});
			StringBuffer sb = new StringBuffer(leaderID + ":");
			for (int j = 0; j < 10; j++) {
				if (j != 0) {
					sb.append(",");
				}
				sb.append(result.get(j).docID2);
				System.out.println(result.get(j).docID1 + " & "
						+ result.get(j).docID2 + " = " + result.get(j).value);
			}
			out.println(sb.toString());
		}

		out.close();
	}

	public SimilarDocuments(Dictionary<String, Integer> dict,
			Hashtable<Integer, String> reverseDict) {

		super(dict, reverseDict);
	}

}
