package li.akermann;

import li.akermann.schema.rambler.SchemaRambler;
import org.apache.kafka.common.serialization.Serializer;

public record SchemaProps<T>(
        String schemaString,
        Serializer<T> serializer,
        SchemaRambler.RandomGenerator<T> randomGenerator) {
}
