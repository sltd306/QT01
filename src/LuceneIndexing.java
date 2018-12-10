
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class LuceneIndexing {

	public static String propFileName = "resources/config.properties";
	public static int countWeb = 0;
	

	/** Index all text files under a directory. */
	public static void main(String[] args) throws IOException {

		// Property Datei lesen
		Properties prop = new Properties();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = cl.getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		String indexPath = prop.getProperty("indexPath");
		String docsPath = prop.getProperty("docsPath");

		boolean create = true;

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path"+ '\n');
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			System.out.println("1. Indexing Website" + '\n');
			System.out.println("2. Meta-Info" + '\n');
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			// int eingabe = br.read();

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);
			writer.close();

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	   * Indexes the given file using the given writer, or if a directory is given,
	   * recurses over files and directories found under the given directory.
	   * 
	   * NOTE: This method indexes one document per input file.  This is slow.  For good
	   * throughput, put multiple documents into your input file(s).  An example of this is
	   * in the benchmark module, which can create "line doc" files, one document per line,
	   * using the
	   * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	   * >WriteLineDocTask</a>.
	   *  
	   * @param writer Writer to the index where the given file/dir info will be stored
	   * @param path The file to index, or the directory to recurse into to find files to index
	   * @throws IOException If there is a low-level I/O error
	   */
	  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
	    if (Files.isDirectory(path)) {
	    	
	      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	    	  
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	        	
	          try {
	        	  String ext = FilenameUtils.getExtension(file.toString());
	        	  if(ext.equals("htm")) {
	        		  
	        		  
	        		  indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
	        		  
	        	  }
	        	  
	          } catch (IOException ignore) {
	            // don't index files that can't be read.
	          }
	          return FileVisitResult.CONTINUE;
	        }
	      });
	    } else {
	    	
	      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	    }
	    System.out.println(countWeb + " Website updated ...");
	  }

	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
		try (InputStream stream = Files.newInputStream(file)) {
			// make a new, empty document
			Document doc = new Document();

			// Add creation time
			BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
			Field dateField = new StringField("date", attr.creationTime().toString(), Field.Store.YES);
			doc.add(dateField);
			System.out.println("date: " + attr.creationTime().toString() + '\n');

			// Add the size of the file
			File fileFile = file.toFile();
			long sizeFile = fileFile.length();
			Field sizeField = new StringField("size", Long.toString(sizeFile), Field.Store.YES);
			doc.add(sizeField);
			System.out.println("size: " + Long.toString(sizeFile) + '\n');

			// Add the author
			// Check if it has meta tag for author
			org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(new File(file.toString()), "utf-8");
			if (jsoupDoc.select("meta[name=author]").size() > 0) {
				String author = jsoupDoc.select("meta[name=author]").first().attr("content");
				Field authorField = new StringField("author", author, Field.Store.YES);
				doc.add(authorField);
				System.out.println("author: " + author + '\n');
			}

			// Add the title
			String title = jsoupDoc.title();
			Field titleField = new StringField("title", title, Field.Store.YES);
			doc.add(titleField);
			System.out.println("title: " + title + '\n');

			// Change file to text
			Scanner scanner = new Scanner(fileFile);
			String text = scanner.useDelimiter("\\A").next();
			scanner.close();

			// Parse the string
			org.jsoup.nodes.Document encodingDoc = Jsoup.parse(text);

			Charset charset = encodingDoc.charset();
			String encoding = charset.name();

			if (encoding != null) {
				Field encodingField = new StringField("encoding", encoding, Field.Store.YES);
				System.out.println("encoding: " + encoding + '\n');
				doc.add(encodingField);
			}

			// Add the contents of the file to a field named "contents". Specify a Reader,
			// so that the text of the file is tokenized and indexed, but not stored.
			// Note that FileReader expects the file to be in UTF-8 encoding.
			// If that's not the case searching for special characters will fail.
			String body = jsoupDoc.body().text();
			body = validate(body);

			// check body to txt
			BufferedWriter buffWriter = new BufferedWriter(new FileWriter("body.txt", true));
			buffWriter.write(body + " ");
			// writer.close();
			buffWriter.close();

			// change String to InputStream for body
			InputStream inputStream = new ByteArrayInputStream(body.getBytes(Charset.forName("UTF-8")));
			doc.add(new TextField("contents",
					new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));

			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document can be there):
				System.out.println("adding " + file + '\n');
				System.out.println("------------------------------------------------------------");
				writer.addDocument(doc);
			} else {
				// Existing index (an old copy of this document may have been indexed) so
				// we use updateDocument instead to replace the old one matching the exact
				// path, if present:
				System.out.println("updating " + file);
				writer.updateDocument(new Term("path", file.toString()), doc);
			}
			countWeb++;
		}
	}

	// Validierung von Strings, ob die nicht mit Zahlen anfangen
	static private String validate(String string) throws IOException {
		String tmp = "";
		String[] splitString = (string.split("\\s+"));
		for (String s : splitString) {
			if (s.matches("^[a-zA-Z]+.*")) {
				tmp += s + " ";
			}
		}
		return tmp;

	}
}