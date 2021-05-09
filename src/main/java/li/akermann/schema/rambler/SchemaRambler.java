package li.akermann.schema.rambler;

import li.akermann.SchemaProps;

import java.util.Optional;

public interface SchemaRambler<T> {

    /**
     * @param schema Schema string retrieved from confluent schema registry.
     * @param isKey  Flag if {@schema} is a key schema
     * @return Empty if schema string cannot be made sense of (e.g. an xsd schema for an avro implementation and vice versa. Or an empty string etc.).
     * Else return SchemaProps with corresponding Serializer, RandomGenerator etc.
     */
    Optional<SchemaProps<T>> mapSchemaToProps(String schema, boolean isKey);

    /**
     * return next random value of type T when next() is called.
     */
    interface RandomGenerator<T> {
        T next();
    }
}
