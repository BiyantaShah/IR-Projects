import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Biyanta on 15/07/17.
 */
public class RankedListFile {

    private static TransportClient transportClient = null;
    private static String INDEX_NAME = "bpp";

    public static void main (String [] args) throws IOException {

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



        BufferedWriter writer = new BufferedWriter(new FileWriter("Input/RankedListFile"));

        QueryBuilder qb1 = QueryBuilders.queryStringQuery("costa concordia disaster and recovery");

        SearchResponse searchResponse1 = transportClient.prepareSearch(INDEX_NAME)
                .setScroll(new TimeValue(60000))
                .setQuery(qb1)
                .setSize(200)
                .execute().actionGet();
        int i = 0;
        System.out.println(searchResponse1.getHits().getHits().length);
        for (SearchHit searchHit: searchResponse1.getHits().getHits()) {
            i++;
            writer.write("152601 " +searchHit.getId() +" " + i +" \n");

        }

        QueryBuilder qb2 = QueryBuilders.queryStringQuery("south korea ferry disaster");
        SearchResponse searchResponse2 = transportClient.prepareSearch(INDEX_NAME)
                .setScroll(new TimeValue(60000))
                .setQuery(qb2)
                .setSize(200)
                .execute().actionGet();
        i = 0;
        System.out.println(searchResponse2.getHits().getHits().length);
        for (SearchHit searchHit: searchResponse2.getHits().getHits()) {
            i++;
            writer.write("152602 " + searchHit.getId() +" " + i +" \n");

        }

        QueryBuilder qb3 = QueryBuilders.queryStringQuery("Lampedusa migrant shipwreck");
        SearchResponse searchResponse3 = transportClient.prepareSearch(INDEX_NAME)
                .setScroll(new TimeValue(60000))
                .setQuery(qb3)
                .setSize(200)
                .execute().actionGet();
        i = 0;
        System.out.println(searchResponse3.getHits().getHits().length);
        for (SearchHit searchHit: searchResponse3.getHits().getHits()) {
            i++;
            writer.write("152603 " +searchHit.getId() +" " + i +" \n");

        }

        writer.flush();
        writer.close();

    }
}
