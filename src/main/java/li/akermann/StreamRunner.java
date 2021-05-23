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
public record StreamRunner<K, V>(SchemaProps<K> keySchemaProps, SchemaProps<V> valSchemaProps, Config config) {

    public CompletionStage<Done> run(ActorSystem system) {
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
