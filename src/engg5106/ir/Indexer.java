package engg5106.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import engg5106.ir.indexer.Index;
import engg5106.ir.indexer.IndexOptions;

public class Indexer {

	public static void main(String[] args) throws ClassNotFoundException {

		try {
			// Indexer indexer = Indexer.open(new File("index/index1000"));

			Indexer indexer = new Indexer(new File("index/index100"));
			indexer.setOptions(new IndexOptions[] {
					// new IndexOptions("subreddit", IndexOptions.Type.Keyword),
					// new IndexOptions("domain", IndexOptions.Type.Keyword),
					new IndexOptions("title", IndexOptions.Type.Tokenize),
					new IndexOptions("content", IndexOptions.Type.Tokenize) });
			File[] inputs = new File("sample/100/")
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".csv");
						}
					});

			// Read files
			CSVFormat format = CSVFormat.RFC4180.withHeader();
			for (File file : inputs) {
				System.out.println("processing " + file.getName());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), "UTF8"));
				CSVParser parser = new CSVParser(in, format);

				String subreddit = file.getName().split("[.]", 2)[0];
				for (CSVRecord record : parser) {
					Document doc = new Document();
					doc.addField("subreddit", subreddit);
					doc.addField("title", record.get("title"));
					doc.addField(
							"content",
							record.get("selftext").length() > 0 ? record
									.get("selftext") : null);
					doc.addField("num_comments", record.get("num_comments"));
					doc.addField("score", record.get("score"));
					doc.addField("permalink", record.get("permalink"));
					doc.addField("domain", record.get("domain"));
					doc.addField("url", record.get("url"));
					indexer.getIndex().add(doc);
				}
				parser.close();
				in.close();
			}

			System.out.println("Total documents: "
					+ indexer.getIndex().getDocumentCount());
			// indexer.getIndex().listDocuments();
			indexer.save();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("--DONE--");
	}

	protected File indexFile;
	protected Index index;

	Indexer(File f) {
		this.indexFile = f;
		this.index = new Index();
	}

	public void setOptions(IndexOptions[] options) {
		this.index.setOptions(options);
	}

	public Index getIndex() {
		return this.index;
	}

	public void save() throws IOException {

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				this.indexFile + "index"));
		oos.writeObject(this.index);
		oos.close();

		oos = new ObjectOutputStream(new FileOutputStream(this.indexFile
				+ "documents"));
		oos.writeObject(this.index.getDocumentDictionary());
		oos.close();

		oos = new ObjectOutputStream(new FileOutputStream(this.indexFile
				+ "terms"));
		oos.writeObject(this.index.getTermDictionary());
		oos.close();
	}

	@SuppressWarnings("unchecked")
	static public Indexer open(File f) throws IOException,
			ClassNotFoundException {
		ObjectInputStream objectinputstream = new ObjectInputStream(
				new FileInputStream(f  + "index"));
		Index index = (Index) objectinputstream.readObject();
		objectinputstream.close();

		
		objectinputstream = new ObjectInputStream(new FileInputStream(f
				+ "documents"));
		DualHashBidiMap<String, Integer> documents = (DualHashBidiMap<String, Integer>) objectinputstream
				.readObject();
		index.setDocumentDictionary(documents);

		objectinputstream.close();
		
		
		objectinputstream = new ObjectInputStream(new FileInputStream(f
				+ "terms"));
		DualHashBidiMap<String, Integer> terms = (DualHashBidiMap<String, Integer>) objectinputstream
				.readObject();
		index.setTermDictionary(terms);

		objectinputstream.close();

		Indexer indexer = new Indexer(f);
		indexer.index = index;
		index.initialize();

		return indexer;
	}
}
