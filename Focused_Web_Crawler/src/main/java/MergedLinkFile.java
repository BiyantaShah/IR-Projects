import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
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
import java.util.List;

/**
 * Created by Biyanta on 01/07/17.
 */
public class MergedLinkFile {

    private static Client transportClient = null;
    static int count = 0;

    public static void main (String [] args) throws IOException {
        Settings settings = Settings.builder()
                .put("cluster.name", "paulbiypri").build();

        transportClient = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),
                        9300));

        System.out.println("Connected");

        reverseCrawlInlinks();
        reverseCrawlOutlinks();
    }

    private static void reverseCrawlOutlinks() throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("mergedOutlinks.txt"));


        QueryBuilder qb = QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = transportClient.prepareSearch("bpp")
                .setScroll(new TimeValue(60000))
                .setExplain(true)
                .setQuery(qb).execute().actionGet();

        while (true) {

            for (SearchHit hit : scrollResp.getHits().getHits()) {

                List outlinks = (List) hit.getSource().get("out_links");
                StringBuilder builder = new StringBuilder();

                count ++;

                try {
                    for (Object object : outlinks) {
                        builder.append(String.valueOf(object)+" ");
                    }
                }
                catch (NullPointerException e) {
                    builder.append(" ");
                }

                try {
                    writer.write(hit.getId()+"="+builder.toString().trim()+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            scrollResp = transportClient.
                    prepareSearchScroll(scrollResp.getScrollId()).
                    setScroll(new TimeValue(60000)).execute().actionGet();

            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        writer.flush();
        writer.close();
        System.out.println("DOCS "+count);
    }

    private static void reverseCrawlInlinks() throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("mergedInlinks.txt"));


        QueryBuilder qb = QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = transportClient.prepareSearch("bpp")
                .setScroll(new TimeValue(60000))
                .setExplain(true)
                .setQuery(qb).execute().actionGet();

        while (true) {

            for (SearchHit hit : scrollResp.getHits().getHits()) {

                List inlinksList = (List) hit.getSource().get("in_links");
                StringBuilder builder = new StringBuilder();

                count ++;

                for (Object object : inlinksList) {
                    builder.append(String.valueOf(object)+" ");
                }

                try {
                    writer.write(hit.getId()+"="+builder.toString().trim()+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            scrollResp = transportClient.
                    prepareSearchScroll(scrollResp.getScrollId()).
                    setScroll(new TimeValue(60000)).execute().actionGet();

            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        writer.flush();
        writer.close();
        System.out.println("DOCS "+count);
    }
}
