package engg5106.ir.bm25;
import java.util.HashMap;
import java.util.StringTokenizer;

import engg5106.ir.Document;
import engg5106.ir.indexer.Index;

public class bm25 {
	private HashMap<Integer, Integer> qmap; //Query Map

	public bm25() {
		this.qmap = new HashMap<Integer, Integer>();
	}


	public double rsv(Index ind,String query, int docid) {
		double def_rsv = 0.0;
		double k1=1.5;
		double k3=1.5;
		double b =0.75;
		String field = "title";
		Document doc;
		int ld;
		double lave;
		
		int n;
		int dft;
		int tftd;
		int tftq;
		
        StringTokenizer tokens = new StringTokenizer(query);
        qmap.clear();
        while(tokens.hasMoreTokens()) {
        	int q_termid;
        	q_termid = ind.getTermId(tokens.nextToken()); // Take back the term id of query
        	
        	if (!this.qmap.containsKey(q_termid)) 
        	{
        		this.qmap.put(q_termid, 0);
        	}
    		this.qmap.put(q_termid, this.qmap.get(q_termid) + 1);		
        }
        doc = ind.getDocument(docid);
        n = ind.getDocumentCount();
        ld= ind.getTermFrequency(field,99999998,docid);
        System.out.println(ld);
        lave =  ind.getTermFrequency(field,99999999,0);
        lave = lave /n;
        for (int queryTerm : qmap.keySet()) {
        	dft = ind.getDocumentFrequency(field,queryTerm);
        	
        	tftd = ind.getTermFrequency(field,queryTerm, docid);
        	tftq = qmap.get(queryTerm);
        	
        	if(dft != 0)
        		def_rsv += Math.log10(n/dft) * (((k1+1)*(double)tftd) / (k1*((1-b)+b*(ld/lave))+tftd))* (((k3+1)*tftq) / (k3 + tftq));
        	else
        		def_rsv += 0;
        }

		return def_rsv;
	}
	

}
