package engg5106.ir;

import org.mapdb.*;
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

/**
 * Use this class to index reddit posts
 * 
 * @author NTF
 *
 */
public class Indexer {

	public static void main(String[] args) throws ClassNotFoundException {

		System.out.println("Indexer");
		Indexer indexer = new Indexer(new File("index/index10"));
		try {
			// setup the index location
			// Indexer indexer = Indexer.open(new File("index/index1000"));

			// indexer = new Indexer(new File("index/index100"));

			// Index configuration , multiple tiers
			indexer.setOptions(new IndexOptions[] {
					// new IndexOptions("subreddit", IndexOptions.Type.Keyword),
					// new IndexOptions("domain", IndexOptions.Type.Keyword),
					new IndexOptions("title", IndexOptions.Type.Tokenize),
					new IndexOptions("content", IndexOptions.Type.Tokenize) });

			indexer.ready();

			// read file from a directory
			File[] inputs = new File("sample/100/").listFiles(new FilenameFilter() {
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
					String content = record.get("selftext").length() > 0 ? record
							.get("selftext") : "";
					content += " " + record.get("top_20_comments");
					// construct document
					Document doc = new Document();
					doc.addField("subreddit", subreddit);
					doc.addField("title", "[" + subreddit +"] " + record.get("title"));
					doc.addField("content", content);
					doc.addField("num_comments", record.get("num_comments"));
					doc.addField("score", record.get("score"));
					doc.addField("permalink", record.get("permalink"));
					doc.addField("domain", record.get("domain"));
					doc.addField("created_utc", record.get("created_utc"));
					// doc.addField("created_utc", record.get("created_utc"));

					// add to index

					indexer.getIndex().add(doc);
				}
				parser.close();
				in.close();
				indexer.getIndex().db.commit();
				indexer.getIndex().db2.commit();
			}

			// indexer.getIndex().listDocuments();
			indexer.getIndex().debug();
			indexer.save();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			indexer.getIndex().debug();
			try {
				indexer.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	public Index ready() {
		DB db = DBMaker.newFileDB(new File(this.indexFile + "-db"))
				.closeOnJvmShutdown().mmapFileEnableIfSupported()
				.cacheSize(10000000).make();
		DB db2 = DBMaker.newFileDB(new File(this.indexFile + "-db2"))
				.closeOnJvmShutdown().cacheSize(100000)
				.mmapFileEnableIfSupported().make();
		index.setDB(db, db2);

		index.initialize();
		return this.index;
	}
	public Index searchReady() {
		DB db = DBMaker.newFileDB(new File(this.indexFile + "-db"))
				.closeOnJvmShutdown().mmapFileEnableIfSupported()
				.cacheSize(10000000).readOnly().make();
		DB db2 = DBMaker.newFileDB(new File(this.indexFile + "-db2"))
				.closeOnJvmShutdown().cacheSize(100000)
				.mmapFileEnableIfSupported().readOnly().make();
		index.setDB(db, db2);

		index.initialize();
		return this.index;
	}
	public Index getIndex() {
		return this.index;
	}

	public void save() throws IOException {
		System.out.println("DB commit start");
		index.db.commit();

		System.out.println("DB commit end");
		/*
		 * ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
		 * this.indexFile + "-index")); oos.writeObject(this.index);
		 * oos.close();
		 */
	}

	public void optimize() {
		System.out.println("DB optimize start");
		index.db.compact();
		System.out.println("DB optimize end");
		/*
		 * ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
		 * this.indexFile + "-index")); oos.writeObject(this.index);
		 * oos.close();
		 */
	}

	@SuppressWarnings("unchecked")
	static public Indexer open(File f) throws IOException,
			ClassNotFoundException {
		/*
		 * ObjectInputStream objectinputstream = new ObjectInputStream( new
		 * FileInputStream(f + "-index")); Index index = (Index)
		 * objectinputstream.readObject(); objectinputstream.close();
		 * 
		 * objectinputstream = new ObjectInputStream(new FileInputStream(f +
		 * "-documents")); DualHashBidiMap<String, Integer> documents =
		 * (DualHashBidiMap<String, Integer>) objectinputstream .readObject();
		 * index.setDocumentDictionary(documents);
		 * 
		 * objectinputstream.close();
		 * 
		 * objectinputstream = new ObjectInputStream(new FileInputStream(f +
		 * "-terms")); DualHashBidiMap<String, Integer> terms =
		 * (DualHashBidiMap<String, Integer>) objectinputstream .readObject();
		 * index.setTermDictionary(terms); objectinputstream.close();
		 * 
		 * // configure and open database using builder pattern. // all options
		 * are available with code auto-completion. DB db =
		 * DBMaker.newFileDB(new File(f + "-db")).closeOnJvmShutdown()
		 * .encryptionEnable("engg5106").make(); db. index.setDB(db);
		 */
		Indexer indexer = new Indexer(f);
		// indexer.index = index;

		return indexer;

	}
}
