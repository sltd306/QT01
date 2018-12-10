
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

public class WordCounter {
	
	public static String propFileName = "resources/config.properties";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub	
		
		///////////////////////////////////////////////////////////
		//load the properties for index and data directories//////
		Properties prop = new Properties();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = cl.getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		String indexDir = prop.getProperty("indexPath");
		String csvDir = prop.getProperty("csvDir");

		//
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(dir);
        final Fields fields = MultiFields.getFields(reader);
        final Iterator<String> iterator = fields.iterator();
      
        //20181208
        System.out.println("1. CSV-Export.");
        System.out.println("2. Standard-Export");
        System.out.println("Eingabe: ");
        Scanner sc = new Scanner(System.in);
        int eingabe = sc.nextInt();
        switch(eingabe) {
        	case 1:
        		createCSV(iterator, reader,csvDir);
        		break;
        	case 2:
        		printMetaInfo(reader);
        }
        
	}
		
		
        
     static private void createCSV(Iterator<String> iterator, IndexReader reader,String csvDir) throws IOException {
			Date d = new Date();
	        
	        BufferedWriter pw = new BufferedWriter (new FileWriter(csvDir+d.getTime()+".csv"));
	        //long maxFreq = Long.MIN_VALUE;
	        String freqTerm = "";
	        String tmp;
	        while(iterator.hasNext()) {
	            final String field = iterator.next();
	            final Terms terms = MultiFields.getTerms(reader, field);
	            final TermsEnum it = terms.iterator();
	            BytesRef term = it.next();
	            while (term != null) {
	                final long freq = it.totalTermFreq();
	                freqTerm = term.utf8ToString();
	               
	                // System.out.println(freqTerm + ";" + freq);      
	                freqTerm = validate(freqTerm);
	                if(freqTerm != "") {
	                	tmp = freqTerm + ";" + freq;
	                	pw.write(tmp);
	                	pw.newLine();
	                }
	                term = it.next(); 	                
	            }
	        }
	        
	        pw.close();
	        System.out.println("done!");
        }
	   
        
        
     static private void printMetaInfo(IndexReader reader) throws IOException {
 		String title, author, size, date, encoding;
 		Bits liveDocs = MultiFields.getLiveDocs(reader);
 		Scanner sc = new Scanner(System.in);
 		int i = 0;
 		char input = 0;
 		
 		do {
 			if (liveDocs != null && !liveDocs.get(i))
                 continue;

             Document doc = reader.document(i);
             date = doc.get("date");
             size = doc.get("size");
             author = doc.get("author");
             title = doc.get("title");
             encoding = doc.get("encoding");
             System.out.println("Number: " + (i+1));
             System.out.println("date: " + date);
             System.out.println("size: " + size);
             if(author != null) {
             	System.out.println("author: " + author);
             }
             System.out.println("title: " + title);
             System.out.println("encoding: " + encoding);
             System.out.println("\n");
             
             
             if((i+1) % 10 != 0) {
             	i++;
             }
             else {
            	 input = sc.next().charAt(0);        	
             	 if(input == 'n') {
                  	i++;
                  }
                  if(input == 'q') {
                	  System.out.println("Quit ...");
                  	break;
                  }
             }
             
 		} while (i<reader.maxDoc());
 		
 	}
	
	
	//verbessen ??
	static private String validate(String string) throws IOException {
		 String tmp = "";
		 String[] splitString = (string.split("\\s+"));
		 for (String s : splitString) {
	     	if(s.matches("^[a-zA-Z]+.*")) {
	     		tmp += s + " ";
	     	}
	     }
		 return tmp;
	  }
	
	
}
