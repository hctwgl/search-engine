package in.nimbo.moama.database;

import in.nimbo.moama.database.webdocumet.WebDocument;
import in.nimbo.moama.metrics.Metrics;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

import static in.nimbo.moama.util.Constants.*;


public class ElasticWebDaoImp implements WebDao {
    public static final String HTTP = "http";
    private RestHighLevelClient client;
    private String index = "pages";
    private Logger errorLogger = Logger.getLogger("error");
    private IndexRequest indexRequest;
    private BulkRequest bulkRequest;
    private static int added = 0;
    private static final Integer sync = 0;

    public ElasticWebDaoImp() {

        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_HOSTNAME, ELASTIC_PORT, HTTP)));
        indexRequest = new IndexRequest(index);
        bulkRequest = new BulkRequest();
    }


    @Override
    public boolean createTable() {
        return false;
    }

    @Override
    public void put(WebDocument document) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            try {
                builder.startObject();
                {
                    builder.field("pageLink", document.getPagelink());
                    builder.field("pageText", document.getTextDoc());
                }

                builder.endObject();
                bulkRequest.add(indexRequest);
                indexRequest = new IndexRequest(index);
                added++;
            } catch (IOException e) {
                errorLogger.error("ERROR! couldn't add " + document.getPagelink() + " to elastic");
            }
            if (bulkRequest.estimatedSizeInBytes() / 1000000 >= ELASTIC_FLUSH_SIZE_LIMIT ||
                    bulkRequest.numberOfActions() >= ELASTIC_FLUSH_NUMBER_LIMIT) {
                synchronized (sync) {
                    client.bulk(bulkRequest);
                    bulkRequest = new BulkRequest();
                    Metrics.numberOfPagesAddedToElastic = added;
                }
            }
        } catch (IOException e) {
            errorLogger.error("ERROR! Couldn't add the document for " + document.getPagelink());
        }
    }

}
