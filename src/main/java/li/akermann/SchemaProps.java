package li.akermann;

import li.akermann.schema.rambler.SchemaRambler;
import lombok.Builder;
import lombok.Value;
import org.apache.kafka.common.serialization.Serializer;

@Builder
@Value
public class SchemaProps<T> {
    String schemaString;
    Serializer<T> serializer;
    SchemaRambler.RandomGenerator<T> randomGenerator;
}
