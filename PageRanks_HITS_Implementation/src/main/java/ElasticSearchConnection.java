import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by Biyanta on 05/07/17.
 */
public class ElasticSearchConnection {

    private TransportClient transportClient = null;
    private final String INDEX_NAME = "bpp";
    private Set<String> rootSet = new HashSet<String>();

    private Map<String, Set<String>> inlinks = new HashMap<String, Set<String>>();

    private Map<String, Set<String>> outlinks = new HashMap<String, Set<String>>();

    private Map<String,Double> hubScore = new HashMap<String,Double>();

    private Map<String,Double> authorityScore = new HashMap<String,Double>();

    public void connect() {

        Settings settings = Settings.builder()
                .put("cluster.name", "paulbiypri").build();

        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress(new InetSocketTransportAddress
                    (InetAddress.getByName("127.0.0.1"), 9300));

            System.out.println("Connected");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public void fetchAllDocuments() {

        QueryBuilder qb = QueryBuilders.queryStringQuery("maritime disasters");

        SearchResponse searchResponse = transportClient.prepareSearch(INDEX_NAME)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(1000)
                .execute().actionGet();

        System.out.println("Total Documents "+ searchResponse.getHits().getHits().length);

        for (SearchHit searchHit: searchResponse.getHits().getHits()) {
            rootSet.add(searchHit.getId());

            List tempOutLinks = (List) searchHit.getSource().get("out_links");

            List tempInLinks = (List) searchHit.getSource().get("in_links");

            if (tempOutLinks != null)
                rootSet.addAll(tempOutLinks);

            int maxLength = (tempInLinks.size() > 200)? 200: tempInLinks.size();

            rootSet.addAll(tempInLinks.subList(0, maxLength));

            inlinks.put(searchHit.getId(), new HashSet<String>(tempInLinks));
            if (tempOutLinks != null)
                outlinks.put(searchHit.getId(), new HashSet<String>(tempOutLinks));
        }

        System.out.println("Root set size "+ rootSet.size());
//        printRootSetToFile(rootSet);

        for (String doc: rootSet) {
            hubScore.put(doc, 1.0);
            authorityScore.put(doc, 1.0);
        }

        AuthorityHub authorityHub = new AuthorityHub();
        authorityHub.score(inlinks, outlinks, rootSet, hubScore, authorityScore);
    }

    private void printRootSetToFile(Set<String> rootSet) {
        BufferedWriter writer = null;
        try {
             writer = new BufferedWriter(new FileWriter("Output/rootSet.txt"));

             for (String links: rootSet) {
                 writer.write(links +"\n");
             }
             writer.flush();
             writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
