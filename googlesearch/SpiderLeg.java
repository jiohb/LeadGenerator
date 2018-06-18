/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package googlesearch;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author NDBDEV
 */
public class SpiderLeg {
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.

    //private static final String USER_AGENT= "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private static final String USER_AGENT= "Mozilla/5.0 (compatible; Googlebot/2.1";
    private List<String> links = new LinkedList<>();
    private Document htmlDocument;
    
    public static Set<String> matchedPatterns=new HashSet<>();

    /**
     * This performs all the work. It makes an HTTP request, checks the
     * response, and then gathers up all the links on the page. Perform a
     * searchForWord after the successful crawl
     *
     * @param url - The URL to visit
     * @return whether or not the crawl was successful
     */
    public boolean crawl(String url) {
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            this.htmlDocument = htmlDocument;
            if (connection.response().statusCode() == 200) // 200 is the HTTP OK status code
            // indicating that everything is great.
            {
                System.out.println("\n**Visiting** Received web page at " + url);

                if (!connection.response().contentType().contains("text/html")) {
                    System.out.println("**Failure** Retrieved something other than HTML");
                    return false;
                }
                Elements linksOnPage = htmlDocument.select("a[href]");
                System.out.println("Found (" + linksOnPage.size() + ") links");
                if (linksOnPage.size() == 0) {
                    return false;
                }
                for (Element link : linksOnPage) {
                    this.links.add(link.absUrl("href"));
                }
                return true;
            }
            else{
                return false;
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            // We were not successful in our HTTP request
            return false;
        }
    }

    /**
     * Performs a search on the body of on the HTML document that is retrieved.
     * This method should only be called after a successful crawl.
     *
     * @param searchWord - The word or string to look for 
     * @return whether or not the word was found
     */
    public boolean searchForWord(String searchWord) {
        // Defensive coding. This method should only be used after a successful crawl.
        if (this.htmlDocument == null) {
            System.out.println("ERROR! Call crawl() before performing analysis on the document");
            return false;
        }
        System.out.println("Searching for the word " + searchWord + "...");
        String bodyText = this.htmlDocument.body().text();
        Pattern p=Pattern.compile(searchWord);
        Matcher m=p.matcher(bodyText);
        
        while(m.find()){
            //System.out.println(m.group(0));
            matchedPatterns.add(m.group(0));
        }
        return p.matcher(bodyText.toLowerCase()).matches();
//        return bodyText.toLowerCase().contains(searchWord.toLowerCase());
    }

    public List<String> getLinks() {
        return this.links;
    }

}
