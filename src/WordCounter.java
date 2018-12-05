
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class WordCounter {
	
	public static String propFileName = "resources/config.properties";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub	
		
		// Property Datei lesen
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
        
        // Schalter bauen csv or standard ausgeben
        // boolean getCSV = false;
        //if(!getcsv){ bau standard 
        //				sonst csv}
        //
        
        PrintWriter pw = new PrintWriter (new File(csvDir));
        StringBuilder sb = new StringBuilder();
        sb.append("Wort");
        sb.append(',');
        sb.append("Anzahl");
        sb.append('\n');

        //long maxFreq = Long.MIN_VALUE;
        String freqTerm = "";
        while(iterator.hasNext()) {
            final String field = iterator.next();
            final Terms terms = MultiFields.getTerms(reader, field);
            final TermsEnum it = terms.iterator();
            BytesRef term = it.next();
            while (term != null) {
                final long freq = it.totalTermFreq();
                freqTerm = term.utf8ToString();
                // bau 1A save A(freqTerm,freq)
                sb.append(freqTerm);
                sb.append(',');
                sb.append(freq);
                sb.append('\n');
                System.out.println(freqTerm + ";" + freq);         
                term = it.next();
            }
        }
        
        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");
        

        //System.out.println(freqTerm);	    		    
	}
}
