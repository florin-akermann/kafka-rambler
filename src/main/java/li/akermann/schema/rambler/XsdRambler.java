package li.akermann.schema.rambler;

import com.typesafe.config.Config;
import li.akermann.SchemaProps;

import java.util.Optional;

public final class XsdRambler implements SchemaRambler<Object> {

    private final Config config;

    public XsdRambler(Config config) {
        this.config = config;
    }

    @Override
    public Optional<SchemaProps<Object>> mapSchemaToProps(String schema, boolean isKey) {
        return Optional.empty();
    }


}
