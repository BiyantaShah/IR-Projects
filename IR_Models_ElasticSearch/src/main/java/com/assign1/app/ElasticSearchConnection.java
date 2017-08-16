package com.assign1.app;

import com.assign1.indexing.Doc;
import com.google.gson.Gson;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Biyanta on 26/05/17.
 */
public class ElasticSearchConnection {

    public TransportClient transportClient = null;
    private final String INDEX_NAME = "biyanta";
    private final String INDEX_TYPE = "document";
    private final Gson gson = new Gson();

    public void connect() {
        Settings settings = Settings.builder()
                .put("cluster.name", "my-application").build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress(new InetSocketTransportAddress
                    (InetAddress.getByName("127.0.0.1"), 9300));

            System.out.println("Connected");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void createIndex(List<Doc> listOfDocs) {

        if(listOfDocs== null || listOfDocs.isEmpty()){
            System.out.println("No documents to update!");
        }

        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
        for (Doc tempDOC : listOfDocs) {
            bulkRequest.add(transportClient.
                    prepareIndex(INDEX_NAME, INDEX_TYPE, tempDOC.getDOCNO())
                    .setSource(gson.toJson(tempDOC, Doc.class)));


        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            System.out.println("Failure in executing Bulk Request!");
        }
    }

    public Client getClient(){
        return transportClient;
    }


    public void printIds() throws IOException {

        QueryBuilder qb = QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = transportClient.prepareSearch(INDEX_NAME)
                .setScroll(new TimeValue(60000))
                .setExplain(true)
                .setQuery(qb).execute().actionGet(); //100 hits per shard will be returned for each scroll

        int count = 0;

        //Scroll until no hits are returned

        System.out.println(scrollResp.getHits().totalHits());

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("src/main/resources/docIds")));

        while (true) {

            for (SearchHit hit : scrollResp.getHits().getHits()) {
                writer.write(hit.getId().trim()+"="+System.getProperty("line.separator"));
                ++count;
            }

            scrollResp = transportClient.prepareSearchScroll
                    (scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        writer.flush();
        writer.close();
        System.out.println("COUNT + " + count);
    }
}
