package li.akermann.schema.rambler;

import com.typesafe.config.Config;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import li.akermann.SchemaProps;
import org.apache.avro.Schema;
import org.apache.avro.util.RandomData;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public final class AvroRambler implements SchemaRambler<Object> {

    private final Config config;

    public AvroRambler(Config config) {
        this.config = config;
    }

    @Override
    public Optional<SchemaProps<Object>> mapSchemaToProps(String schemaString, boolean isKey) {
        try {
            var schema = new Schema.Parser().parse(schemaString);
            var serializer = new KafkaAvroSerializer();
            serializer.configure(
                    Map.<String, Object>of(SCHEMA_REGISTRY_URL_CONFIG, config.getString(SCHEMA_REGISTRY_URL_CONFIG)),
                    isKey
            );

            var schemaProps = new SchemaProps<>(
                    schemaString,
                    serializer,
                    new RandomAvroData(config, schema)
            );

            return Optional.of(schemaProps);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class RandomAvroData implements RandomGenerator<Object> {

        private final Config config;
        private final Schema schema;
        private Iterator<Object> randomDataIterator;

        public RandomAvroData(Config config, Schema schema) {
            this.config = config;
            this.schema = schema;
            randomDataIterator = initRandomData(schema);
        }

        private Iterator<Object> initRandomData(Schema schema) {
            if (config.hasPath("random.seed")) {
                return new RandomData(schema, 100, config.getInt("random.seed")).iterator();
            } else {
                return new RandomData(schema, 100).iterator();
            }
        }

        @Override
        public Object next() {
            if (!randomDataIterator.hasNext()) {
                randomDataIterator = initRandomData(schema);
            }
            return randomDataIterator.next();
        }
    }
}
