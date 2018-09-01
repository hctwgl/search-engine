package in.nimbo.moama.kafka;

import in.nimbo.moama.configmanager.ConfigManager;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

public class MoamaProducer {
    private final String topic;
    private final Producer<String, String> producer;
    private final Logger errorLogger = Logger.getLogger(this.getClass());
    public MoamaProducer(String topic, String rootAddress) {
        //TODO
        this.topic = topic;
        producer = new KafkaProducer<>(ConfigManager.getInstance().getProperties(rootAddress,true));
    }
    public void pushDocument(String document){
        producer.send(new ProducerRecord<>(topic,"", document));
    }
    public void pushNewURL(String... links) {
        for (String url : links) {
            try {
                String key = new URL(url).getHost();
                producer.send(new ProducerRecord<>(topic, key, url));
            } catch (MalformedURLException e) {
                errorLogger.error("Wrong Exception" + url);
            }
        }
    }

    public void flush() {
        producer.flush();
    }
}
