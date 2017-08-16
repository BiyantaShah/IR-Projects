import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.language.LanguageIdentifier;

/**
 * Created by Biyanta on 14/06/17.
 */
public class WebCrawl {

    private final int MAX_DOCS = 4;

    private Set<String> visited = new LinkedHashSet<String>();
    private Map<String,Set<String>> inlinkSet = new HashMap<String, Set<String>>();

    private int fileCount;
    private int count = 0, totalDocs = 0;

    private BufferedWriter inlinksWriter = null;
    private BufferedWriter outlinksWriter = null;
    private BufferedWriter docWriter = null;

    static Map<String,Long> sleepTime = new HashMap<String, Long>();

    private CustomQueue queue = null;

    public void setUp() {

        queue = new CustomQueue(inlinkSet);

        try {
            
            inlinksWriter = new BufferedWriter(new FileWriter("Documents1/inLinks.txt"));
            outlinksWriter = new BufferedWriter(new FileWriter("Documents1/outLinks.txt"));
            docWriter = new BufferedWriter(new FileWriter("Documents1/"+fileCount+".txt"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void startCrawl() throws Exception {
        
        crawlSeedLinks();

        while (queue.hasNext()) {

            List<Links> linkSet = queue.dequeue();

            for (Links link : linkSet) {

                if (!visited.contains(link.getCanonicalizedUrl())) {
                    Long currentTime = System.currentTimeMillis();
                    if (sleepTime.containsKey(link.getAuthority())) {
                        long sleep = currentTime - sleepTime.get(link.getAuthority());
                        if(sleep < 1000){
                            try {
                                Thread.sleep(sleep);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    sleepTime.put(link.getAuthority(),currentTime);
                    crawlUrl(link);
                }
            }
        }
    }

    private void crawlSeedLinks() throws Exception {

        String seed0 = "http://en.wikipedia.org/wiki/List_of_maritime_disasters";

        String seed1 = "http://www.telegraph.co.uk/news/worldnews/europe/italy/10312026/Costa-Concordia-" +
                "recovery-timeline-of-cruise-ship-disaster.html";
        String seed2 = "http://en.wikipedia.org/wiki/Costa_Concordia";
        String seed3 = "http://en.wikipedia.org/wiki/Costa_Concordia_disaster";

        Links link0 = new Links(seed0,canonicalizedURL(seed0));link0.setDepth(0);
        Links link1 = new Links(seed1,canonicalizedURL(seed1));link1.setDepth(0);
        Links link2 = new Links(seed2,canonicalizedURL(seed2));link2.setDepth(0);
        Links link3 = new Links(seed3,canonicalizedURL(seed3));link3.setDepth(0);

        crawlUrl(link0);
        crawlUrl(link1);
        crawlUrl(link2);
        crawlUrl(link3);
    }

    private void crawlUrl(Links link) {

        try {
            crawl(link.getRawUrl(),link.getCanonicalizedUrl(),link.getDepth());
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
        visited.add(link.getCanonicalizedUrl());
        
    }

    private void crawl(String rawUrl, String canonicalizedUrl, int depth) throws IOException {

        Document doc = Jsoup.connect(rawUrl).get();

        cleanHTML(doc,"div#mw-head");
        cleanHTML(doc,"div#toc");
        cleanHTML(doc,"div#mw-panel");
        cleanHTML(doc,"div#catlinks");
        cleanHTML(doc,"div#footer");

        Elements elements = doc.getElementsByTag("a");
        Iterator<Element> elementIter = elements.iterator();
        Set<String> tempOutlinksSet = new HashSet<String>();
        System.out.println(totalDocs + " URL: " + rawUrl);

        StringBuilder sb = new StringBuilder(doc.body().text().toLowerCase());

        if (! languageOkay(doc.body().text().toLowerCase())) {
            return;
        }

        int relevantContent = calculateRelevance(sb);

        Set<String> tempCanonicalizedUrl = new HashSet<String>();

        while (elementIter.hasNext()) {


            try {
                Element element = elementIter.next();

                Links links = new Links(element.attr("abs:href"));

                if (tempCanonicalizedUrl.contains(links.getCanonicalizedUrl())) {
                    continue;
                }
                else {
                    if (!((links.getCanonicalizedUrl().trim().length()) == 0))
                        tempCanonicalizedUrl.add(links.getCanonicalizedUrl());
                    else
                        continue;
                }

                if ((urlValidated(links.getCanonicalizedUrl(), links.getRawUrl())) && relevantContent >= 10) {

                    links.setDepth(depth + 1);
                    links.setRelevance(relevantContent);
                    queue.enqueue(links);

                    tempOutlinksSet.add(links.getCanonicalizedUrl());

                    if (inlinkSet.containsKey(links.getCanonicalizedUrl())) {
                        inlinkSet.get(links.getCanonicalizedUrl()).add(canonicalizedUrl);
                    }
                    else {
                        Set<String> tempLinkSet = new HashSet<String>();
                        tempLinkSet.add(canonicalizedUrl);
                        inlinkSet.put(links.getCanonicalizedUrl(), tempLinkSet);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        Set<String> tempAuth = new HashSet<String>();
        tempAuth.add("Biyanta");

        Doc document = new Doc();
        document.setDOCNO(canonicalizedUrl);
        document.setHtmlSource(doc.outerHtml());
        document.setTEXT(doc.body().text());
        document.setOutlinks(tempOutlinksSet);
        document.setAuthors(tempAuth);
        document.setUrl(rawUrl);
        document.setDEPTH(depth);
        
        try {
            document.setHEAD(doc.title()); 
        }
        catch (Exception e) {
            document.setHEAD(null);
        }

        printDocToFile(document);


    }

    private int calculateRelevance(StringBuilder sb) {

        int relevance = 0;

        Pattern p = Pattern.compile("disaster|maritime|marine|help|accident");

        Matcher m = p.matcher(sb);

        while (m.find()) {
            relevance ++;
        }

        return relevance;
    }

    private void printDocToFile(Doc doc) throws IOException {

        if (count == 2) {
          ++fileCount;
          docWriter.flush();
          docWriter.close();
          docWriter = new BufferedWriter(new FileWriter("Documents1/"+fileCount+".txt"));
          count = 0;
        }
        docWriter.write(getDocument(doc));
        docWriter.write("\n");
        ++count;
        ++totalDocs;

        writeOutlinks(doc);
        
        if (totalDocs == MAX_DOCS) {
            writeToFile();
            close();
            System.exit(1);
        }
    }

    private void writeOutlinks(Doc doc) throws IOException {

        for (String outlink: doc.getOutlinks()) {
            outlinksWriter.write(doc.getDOCNO()+"="+outlink+"\n");
        }
    }

    private void writeToFile() throws IOException {

        for (String key: inlinkSet.keySet()) {

            Set<String> inlinks = inlinkSet.get(key);
            inlinksWriter.write(key+"="+getLinks(inlinks)+"\n");
        }
    }

    private String getDocument(Doc doc) {

        StringBuilder sb = new StringBuilder();

        sb.append("<DOC>"+"\n");
        sb.append("<DOCNO>"+doc.getDOCNO()+"</DOCNO>"+"\n");
        sb.append("<TITLE>"+doc.getHEAD()+"</TITLE>"+"\n");
        sb.append("<TEXT>"+doc.getTEXT()+"</TEXT>"+"\n");
        sb.append("<OUTLINKS>"+getLinks(doc.getOutlinks())+"</OUTLINKS>"+"\n");
        sb.append("<DEPTH>"+doc.getDEPTH()+"</DEPTH>"+"\n");
        sb.append("<URL>"+doc.getUrl()+"</URL>"+"\n");
        sb.append("<AUTHORS>"+getLinks(doc.getAuthors())+"</AUTHORS>"+"\n");
        sb.append("<HTML_SOURCE>"+doc.getHtmlSource()+"</HTML_SOURCE>"+"\n");
        sb.append("</DOC>");
        
        return sb.toString();
    }


    private String getLinks(Set<String> links) {

        StringBuilder sb = new StringBuilder();
        
        for (String link : links) {
            
            sb.append(link+" ");
        }
        return sb.toString().trim();
    }

    private boolean urlValidated(String canonicalizedUrl, String rawUrl) {

        return !isUrlVisited(canonicalizedUrl)
                && !isAdUrl(rawUrl)
                && isRobotPermitted(rawUrl);
    }

    private boolean isRobotPermitted(String rawUrl) {

        RobotCheck robotCheck = new RobotCheck();
        return robotCheck.isRobotPermitted(rawUrl);
    }

    private boolean isAdUrl(String rawUrl) {

        Ads ads = new Ads();
        return ads.isAdUrl(rawUrl);
    }

    private boolean isUrlVisited(String canonicalizedUrl) {

        if(visited.contains(canonicalizedUrl)) {

            if (inlinkSet.containsKey(canonicalizedUrl)) {
                inlinkSet.get(canonicalizedUrl).add(canonicalizedUrl);
            }
            else {
                Set<String> tempLinkSet = new HashSet<String>();
                tempLinkSet.add(canonicalizedUrl);
                inlinkSet.put(canonicalizedUrl, tempLinkSet);
            }
            return true;
        }
        return false;

    }

    private boolean languageOkay(String text) {
        try{
            return new LanguageIdentifier(text).
                    getLanguage().
                    equalsIgnoreCase("en");
        }
        catch(Exception e){
            return false;
        }
    }

    private void cleanHTML(Document doc, String tag) {

        try{

            doc.select(tag).first().remove();
        }
        catch(Exception e){
            System.out.println(tag + " not present in the document");
        }
    }

    public String canonicalizedURL(String link) throws Exception {
        StringBuilder sb = new StringBuilder();

        if (!((link.trim().length() == 0) || (link == null))) {

            try {

                URL tempUrl = new URL(link.split("#")[0]);


                sb.append("http://");

                sb.append(tempUrl.getAuthority().toLowerCase());

                if(tempUrl.getPort() != -1 && tempUrl.getProtocol().equalsIgnoreCase("http") &&
                        tempUrl.getPort() != 80) {

                    sb.append(":"+ tempUrl.getPort());
                }
                else if(tempUrl.getPort() != -1 && tempUrl.getProtocol().equalsIgnoreCase("https") &&
                        tempUrl.getPort() != 443) {

                    sb.append(":"+ tempUrl.getPort());
                }

                sb.append(tempUrl.getPath().replaceAll("//", "/"));

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public void close() {

        try {
            docWriter.flush();
            docWriter.close();

            outlinksWriter.flush();
            outlinksWriter.close();

            inlinksWriter.flush();
            inlinksWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
