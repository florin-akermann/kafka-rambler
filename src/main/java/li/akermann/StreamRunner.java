package li.akermann;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Producer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.CompletionStage;

@Slf4j
public class StreamRunner<K, V> {

    private final SchemaProps<K> keySchemaProps;
    private final SchemaProps<V> valSchemaProps;
    private final Config config;

    public StreamRunner(SchemaProps<K> keySchemaProps, SchemaProps<V> valSchemaProps, Config config) {
        this.keySchemaProps = keySchemaProps;
        this.valSchemaProps = valSchemaProps;
        this.config = config;
    }

    CompletionStage<Done> run(ActorSystem system) {
        return Source.repeat(1)
                .map(e -> toRandomProducerRecord())
                .throttle(config.getInt("ramble.elements"), config.getDuration("ramble.duration"))
                .toMat(Producer.plainSink(ProducerSettings.create(
                        config.getConfig("akka.kafka.producer"),
                        keySchemaProps.serializer(),
                        valSchemaProps.serializer()
                )), Keep.right())
                .run(system);
    }

    private ProducerRecord<K, V> toRandomProducerRecord() {
        return new ProducerRecord<>(
                config.getString("topic.target"),
                keySchemaProps.randomGenerator().next(),
                valSchemaProps.randomGenerator().next()
        );
    }

}
