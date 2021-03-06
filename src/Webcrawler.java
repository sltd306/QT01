import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class Webcrawler {

	public static String propFileName = "resources/config.properties";

	
	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException,IOException {
		// TODO Auto-generated method stub
		// 
		
		// Property Datei lesen
		Properties prop = new Properties();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = cl.getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		String proxy = prop.getProperty("proxy");
		String sPort = prop.getProperty("port");
		String txtWeb = prop.getProperty("docsPath");
		String url = prop.getProperty("url");
		//int port = Integer.parseInt(sPort);
		
		//
		//
		//WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60);
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(2000);
        webClient.getOptions().setUseInsecureSSL(false);
        // overcome problems in JavaScript
        webClient.getOptions().setThrowExceptionOnScriptError(true);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		
        
        /*
		HtmlPage page = webClient.getPage(url);
		String pageContent = page.asText();
		Date d = new Date();
		File txtFile= new File(txtWeb);
		FileWriter fw = new FileWriter(txtWeb+d.getTime()+".txt");
		fw.write(pageContent);	
		System.out.println("Webcrawler done !!");
		webClient.close();
		fw.close();
		*/
		
		//20181207        
        HtmlPage page = webClient.getPage(url);      
		String titel = page.getTitleText()+".html";
		File file = new File(txtWeb + titel);
		// file exists shoul be overwrite
		page.save(file);
		webClient.close();
			
		System.out.println("Website: " + titel + " done!");
		
		
	
	}
}