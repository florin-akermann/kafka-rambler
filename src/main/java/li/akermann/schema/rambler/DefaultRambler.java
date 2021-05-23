package li.akermann.schema.rambler;

import com.typesafe.config.Config;
import li.akermann.SchemaProps;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Optional;
import java.util.Random;

public class DefaultRambler implements SchemaRambler<Object> {

    private final Config config;
    private final Random random;

    public DefaultRambler(Config config) {
        this.config = config;
        this.random = getRandom();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<SchemaProps<Object>> mapSchemaToProps(String schema, boolean isKey) {
        return Optional.of((SchemaProps<Object>) defaultProps());
    }

    private SchemaProps<?> defaultProps() {
        return new SchemaProps<>(
                null,
                new StringSerializer(),
                this::getRandomString
        );
    }

    private String getRandomString() {
        var min = config.getInt("random.string.length.min");
        var max = config.getInt("random.string.length.max");
        var randInt = random.nextInt((max - min) + 1) + min;
        return RandomStringUtils.random(randInt, 0, 0, true, true, null, random);
    }

    private Random getRandom() {
        if (config.hasPath("random.seed")) {
            return new Random(config.getInt("random.seed"));
        } else {
            return new Random();
        }
    }
}
