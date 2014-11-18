package engg5106.ir.bm25;

import java.util.HashMap;
import java.util.StringTokenizer;

import engg5106.ir.Document;
import engg5106.ir.indexer.Index;

public class bm25 {
	private HashMap<Integer, Integer> query_map; // Query Map

	public bm25() {
		this.query_map = new HashMap<Integer, Integer>();
	}

	public double rsv(Index ind, HashMap<Integer, Integer> qmap, int docid,String field) {
		double def_rsv = 0.0;
		double k1 = 1.5;
		double k3 = 1.5;
		double b = 0.75;
		
		Document doc;
		int ld;
		double lave;

		int n;
		int dft;
		int tftd;
		int tftq;

		query_map = qmap;
		
		n = ind.getDocumentCount();
		String sld = ind.getDocument(docid).getField(field +"_length");
		if (sld != null)
			ld = Integer.parseInt(sld);
		else
			ld = 0;
		System.out.println(ld);
		
		lave = ind.getAverageDocumentLength("title");
		lave = lave / n;
		
		for (int queryTerm : query_map.keySet()) {
			dft = ind.getDocumentFrequency(field, queryTerm); //// Document Frequency
			
			tftd = ind.getTermFrequency(field, queryTerm, docid); // Document Term Frequency
			tftq = query_map.get(queryTerm);  // Query Term Frequency
			if (dft != 0)
				def_rsv += Math.log10(n / dft)
						* (((k1 + 1) * (double) tftd) / (k1
								* ((1 - b) + b * (ld / lave)) + tftd))
						* (((k3 + 1) * tftq) / (k3 + tftq));
			else
				def_rsv += 0;
		}

		return def_rsv;
	}

}
