import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Biyanta on 02/08/17.
 */
public class ElasticSearchConnection {

    public TransportClient transportClient = null;
    public String INDEX_NAME = "spam_dataset";
    public String INDEX_TYPE = "document";
    private Gson gson = new Gson();

    public static Map<String,String> spamHamMap = new HashMap<String, String>();
    Map<String,String> testIDSMap = new HashMap<String, String>();
    Map<String,String> trainIDSMap = new HashMap<String, String>();

    public static final List<InMail> inmail = new LinkedList<InMail>();

    int indexSize = 0;

    public void connect() {

        Settings settings = Settings.builder()
                .put("cluster.name", "my-application").build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress(new InetSocketTransportAddress
                    (InetAddress.getByName("127.0.0.1"), 9300));

            System.out.println("Connected");

        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void mapSpamHam() throws IOException {


        BufferedReader reader = new BufferedReader(new FileReader("index"));
        String line = new String();

        while ((line = reader.readLine()) != null) {

            String [] spamHamData = line.split(" ");
            String fileName = spamHamData[1].trim().substring(8);
            spamHamMap.put(fileName, spamHamData[0].trim());

        }

        reader.close();

    }

    public void loadTestTrainData() throws IOException {

        loadData (new File ("trainIDS.txt"), trainIDSMap);
        loadData (new File ("testIDS.txt"), testIDSMap);
    }

    private void loadData(File fileName, Map<String, String> IDMap) throws IOException {


        BufferedReader reader =  new BufferedReader(new FileReader(fileName));

        String line = new String();

        while ((line = reader.readLine()) != null) {

            String[] mapData = line.split(" ");
            IDMap.put(mapData[0].trim(), mapData[1].trim());

        }

        reader.close();

    }

    public void indexFileOnES(int id,
                              String text,
                              String label,
                              String fileName, String split,
                              boolean indexAll) {

        if (!indexAll) {
            inmail.add(new InMail(String.valueOf(id), fileName, text, label, split));

            if (inmail.size() < 1000)
                return;
        }

        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();

        Iterator<InMail> docIter = inmail.iterator();
        while(docIter.hasNext()){
            InMail inmailData = docIter.next();
            bulkRequest.add(transportClient.
                    prepareIndex(INDEX_NAME, INDEX_TYPE, inmailData.getId())
                    .setSource(gson.toJson(inmailData,InMail.class)));
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            System.out.println("Error in executing Bulk Request!");
        }

        indexSize = indexSize + inmail.size();
        System.out.println("Docs Indexed "+indexSize);
        inmail.clear();
    }


    public void close() {
        if (transportClient != null)
            transportClient.close();
    }


}
