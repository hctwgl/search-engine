package in.nimbo.moama.kafka;

import in.nimbo.moama.document.WebDocument;
import org.junit.Test;

public class KafkaManagerTest {

    @Test
    public void documentToJson() {
        KafkaManager kafkaManager = new KafkaManager("");
        System.out.println(kafkaManager.documentToJson(new WebDocument()));
    }
}