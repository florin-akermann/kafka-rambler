package li.akermann;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import li.akermann.schema.rambler.AvroRambler;
import li.akermann.schema.rambler.DefaultRambler;
import li.akermann.schema.rambler.SchemaRambler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Slf4j
public class KafkaRambler {

    private final Config config;

    private final ActorSystem system;
    private final SchemaFetcher schemaFetcher;
    private final List<SchemaRambler<Object>> schemaRamblers;

    public KafkaRambler() {
        config = ConfigFactory.load();
        system = ActorSystem.create();
        schemaFetcher = new SchemaFetcher(system);
        schemaRamblers = initSchemaRamblers();
    }

    //add other ramblers here, e.g. XsdRambler or JsonRambler
    private List<SchemaRambler<Object>> initSchemaRamblers() {
        return List.of(
                new AvroRambler(config),
                new DefaultRambler(config) // should be the last in the list as a fallback
        );
    }

    public void run() throws ExecutionException, InterruptedException, TimeoutException {

        var keyProps = schemaFetcher.fetch(config.getString("topic.schema-source") + "-key")
                .thenApply(schema -> derivePropsFrom(schema, true))
                .toCompletableFuture().get(config.getDuration("request.timeout", TimeUnit.SECONDS), TimeUnit.SECONDS);

        var valProps = schemaFetcher.fetch(config.getString("topic.schema-source") + "-value")
                .thenApply(schema -> derivePropsFrom(schema, false))
                .toCompletableFuture().get(config.getDuration("request.timeout", TimeUnit.SECONDS), TimeUnit.SECONDS);


        new StreamRunner<>(keyProps, valProps, config).run(system)
                .whenComplete((done, throwable) -> {
                    if (throwable != null) {
                        log.warn("stream completed unexpectedly", throwable);
                        System.exit(1);
                    } else {
                        log.info("stream completed");
                        System.exit(0);
                    }
                });
    }

    private SchemaProps<Object> derivePropsFrom(String s, boolean isKey) {
        return schemaRamblers.stream()
                .map(rambler -> rambler.mapSchemaToProps(s, isKey))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        new KafkaRambler().run();
    }


}
