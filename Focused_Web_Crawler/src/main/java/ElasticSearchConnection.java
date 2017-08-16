import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by Biyanta on 21/06/17.
 */
public class ElasticSearchConnection {

    private Client transportClient = null;
    private final String INDEX_NAME = "bpp5";
    private final String INDEX_TYPE = "document";
    private final Gson gson = new Gson();

    private final Map<String,Set<String>> inlinksSet = new HashMap<String,Set<String>>();

    private final HttpClient httpClient = HttpClientBuilder.create().build();

    private static PrintWriter printWriter;

    private static Set<String> docNos;

    private File dataFolder = new File("Documents1/");

    public void connect() {
        Settings settings = Settings.builder()
                .put("cluster.name", "paulbiypri").build();
//        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),
                            9300));

            System.out.println("Connected");

//            printAllDocs();
            loaddocNoList();
            loadLinkGraphFile();
            System.out.println("Graph loaded");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loaddocNoList() throws IOException {

        docNos = new HashSet<String>();

        BufferedReader reader = new BufferedReader(new FileReader("Documents1/docNo.txt"));

        String line = new String();
        while ((line = reader.readLine()) != null) {
            docNos.add(line.trim());
        }

    }

    private void printAllDocs() throws IOException {

        printWriter = new PrintWriter("Documents1/docNo.txt");

        String docPattern = "<DOC>\\s(.+?)</DOC>";
        String docNoPattern = "<DOCNO>(.+?)</DOCNO>";

        int fileCount = 0;
        while (fileCount <= 1) {

            File mFile = new File("Documents1/"+fileCount+".txt");
            String str = FileUtils.readFileToString(mFile);

            Pattern DOCpattern = Pattern.compile(docPattern, Pattern.DOTALL);
            Matcher DOCmatcher = DOCpattern.matcher(str);
            int i = 1;

            while (DOCmatcher.find()) {
                String doc = DOCmatcher.group(1);

                final Pattern DOCNOPattern = Pattern.compile(docNoPattern);
                final Matcher DOCNOMAtcher = DOCNOPattern.matcher(doc);

                if (DOCNOMAtcher.find()) {
                    String docNo = DOCNOMAtcher.group(1).trim();
                    printWriter.println(docNo);
                }
            }
            fileCount = fileCount + 1;
        }

        printWriter.close();
    }

    private void loadLinkGraphFile() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("Documents1/inlinks.txt"));

            String line = new String();

            while ((line = reader.readLine()) != null) {
                String[] links = line.split("=");

                try {

                    if (docNos.contains(links[0])) {
                        inlinksSet.put(links[0], getLinks(links[1]));
                    }
                }
                catch (ArrayIndexOutOfBoundsException ar) {
                    inlinksSet.put(links[0], Collections.EMPTY_SET);
                }

            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Map Size "+inlinksSet.size());
    }

    private Set<String> getLinks(String links) {

        if (links.length() == 0 || links == null) {
            return null;
        }

        String[] outLinks = links.split(" ");
        Set<String> linkSet = new LinkedHashSet<String>();

        for (String link: outLinks) {
            linkSet.add(link.trim());
        }

        return linkSet;
    }

    public void indexDocs() throws IOException {


        for(File file: dataFolder.listFiles()) {
            if (!file.getName().toLowerCase().contains("link")
                    && !file.getName().toLowerCase().contains("_store")
                    && !file.getName().toLowerCase().contains("docno")) {
                    System.out.println(file.getName());
                    List<Doc> listOfDocs = parseFile(file);

                    System.out.println("created index");
                    indexAllDocs(listOfDocs);
                    listOfDocs.clear();
            }
        }
    }


    private void indexAllDocs(List<Doc> listOfDocs) {

        if(listOfDocs== null || listOfDocs.isEmpty()){
            System.out.println("No documents to update!");
        }

        for (Doc doc : listOfDocs) {

            if (doc != null) {

                QueryBuilder qb = QueryBuilders.matchQuery("_id", doc.getDOCNO());

                SearchResponse searchResponse = transportClient.prepareSearch(INDEX_NAME)
                        .setQuery(qb).execute().actionGet();

                if (searchResponse.getHits().getHits().length == 1) {
                    updateIndex(doc, searchResponse);
                }
                try{
                    IndexResponse indexResponse = transportClient.prepareIndex(
                            INDEX_NAME, INDEX_TYPE)
                            .setId(doc.getDOCNO())
                            .setSource(gson.toJson(doc)).get();

                } catch(Exception e){
                    e.printStackTrace();
                }

            }
        }
        System.out.println("Total Documents indexed "+listOfDocs.size());
    }

    private void updateIndex(Doc doc, SearchResponse searchResponse) {

        SearchHit searchHit =  searchResponse.getHits().getHits()[0];
        List temp = (List) searchHit.getSource().get("out_links");
        if (temp != null) {
            doc.getOutlinks().addAll(temp);
        }

        temp = (List) searchHit.getSource().get("in_links");
        if (temp != null) {
            doc.getInlinks().addAll(temp);
        }

        temp = (List) searchHit.getSource().get("author");
        if (temp != null) {
            doc.getAuthors().addAll(temp);
        }


    }

    private void updateHtmlForDoc(Doc doc) {

        HttpGet request = new HttpGet(doc.getUrl());
        HttpResponse httpResponse;

        try {

            httpResponse = httpClient.execute(request);

            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                StringBuilder headerStringBuilder = new StringBuilder();
                Header[] headers = httpResponse.getAllHeaders();

                for (Header header : headers) {

                    headerStringBuilder.append(header.toString());
                }

                doc.setHttpHeader(headerStringBuilder.toString());

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

                StringBuilder htmlSourceCode = new StringBuilder();

                String line = new String();

                while ((line = reader.readLine()) != null) {
                    htmlSourceCode.append(line);
                }

                doc.setHtmlSource(htmlSourceCode.toString());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Doc> parseFile(File file) throws IOException {
        System.out.println("File name "+ file);
        Document document = Jsoup.parse(file, "UTF-8");

        System.out.println("document parsed");

        Elements docs = document.getElementsByTag("DOC");
        System.out.println("Tagged elements ");

        List<Doc> listOfDocs = new ArrayList<Doc>();
        Iterator<Element> docsIterator = docs.iterator();
        System.out.println("document parsed " + docs.size());


        while(docsIterator.hasNext()){
            Element element = docsIterator.next();
            Doc createdDoc = createDocument(element);

            if(createdDoc!= null){
                listOfDocs.add(createdDoc);
            }
        }
        System.out.println("end of parse file");

        return listOfDocs;
    }

    private Doc createDocument(Element element) {

        Doc doc = new Doc();
        String[] tags = {"DOCNO","TEXT", "DEPTH", "URL", "OUTLINKS", "TITLE", "HTML_SOURCE"};

        for(String tag : tags){
            Elements eleTag = element.getElementsByTag(tag);
            Iterator<Element> eleIter = eleTag.iterator();

            while(eleIter.hasNext()){
                String textValue = new String();
                if (tag.equals("HTML_SOURCE")) {
                     textValue = eleIter.next().toString();
                     textValue = textValue.replaceAll("<html_source>", "");
                     textValue = textValue.replaceAll("</html_source>", "");
                }
                else {
                    textValue = eleIter.next().text();
                }


                if(tag.equals("DOCNO"))
                    doc.setDOCNO(textValue);

                else if (tag.equals("TITLE"))
                    doc.setHEAD(textValue);

                else if(tag.equals("TEXT"))
                    doc.setTEXT(textValue);

                else if (tag.equals("DEPTH"))
                    doc.setDEPTH(Integer.parseInt(textValue));

                else if (tag.equals("URL"))
                    doc.setUrl(textValue);

                else if (tag.equals("OUTLINKS"))
                    doc.setOutlinks(getLinks(textValue));

                else if (tag.equals("HTML_SOURCE"))
                    doc.setHtmlSource(textValue);
            }
        }

        Set<String> tempSet = inlinksSet.get(doc.getDOCNO());

        if (tempSet != null)
            doc.getInlinks().addAll(tempSet);

        doc.getAuthors().add("Biyanta");

        return doc;
    }
}
