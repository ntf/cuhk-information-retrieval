package engg5106.ir.bm25;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
		//System.out.println(ld);

		double doc_time = Double.parseDouble(ind.getDocument(docid).getField("created_utc"));
		int score = Integer.parseInt(ind.getDocument(docid).getField("score"));
		
		lave = ind.getAverageDocumentLength(field);
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
		double month = Math.abs((( System.currentTimeMillis() / 1000l))- doc_time) / 2592000;
		
		//NumberFormat formatter = new DecimalFormat("#.#######");  
		
		//System.out.println("Org :" + formatter.format(def_rsv));
		month-=17;
		
		if (score >0 && score<100)
		{
			def_rsv += def_rsv*0.001*score;
		}
		else
		{
			def_rsv += def_rsv*0.1;
		}
		
		//System.out.println("Month :" + month + " month :" + formatter.format(def_rsv*0.25*(1/month)));
		def_rsv = def_rsv  + def_rsv*0.25*(1/month);
		//System.out.println("New :" + formatter.format(def_rsv));
		//System.out.println();
		return def_rsv;
	}

}
