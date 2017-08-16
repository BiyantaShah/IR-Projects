import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Biyanta on 29/06/17.
 */
public class IndividualIndexedDocs {

    private static Client transportClient = null;

    public static void main (String[] args) throws IOException {
        Settings settings = Settings.builder()
                .put("cluster.name", "paulbiypri").build();

        transportClient = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),
                        9300));

        System.out.println("Connected");

        Set<String> docNos = new HashSet<String>();

        BufferedReader reader = new BufferedReader(new FileReader("docNo.txt"));

        String line = new String();
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            docNos.add(line.trim());
        }
        System.out.println("loaded");
        int count = 0;

        for (String doc : docNos) {

            QueryBuilder qb = QueryBuilders.matchQuery("_id", doc);

            SearchResponse searchResponse = transportClient.prepareSearch("bpp")
                    .setQuery(qb).execute().actionGet();

            SearchHit searchHit = null;
            try {
                searchHit = searchResponse.getHits().getHits()[0];
            }
            catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }

            List temp  = (List)searchHit.getSource().get("author");

            if (temp.contains("Biyanta") && temp.size() == 1) {
                count ++;
            }

        }
        System.out.println("Documents indexed by me "+ count);
    }
}
