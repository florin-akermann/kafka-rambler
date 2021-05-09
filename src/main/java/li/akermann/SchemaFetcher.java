package li.akermann;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SchemaFetcher {

    private final ObjectMapper mapper;
    private final ActorSystem system;

    public SchemaFetcher(ActorSystem system) {
        this.system = system;
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        this.mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
    }

    public CompletionStage<String> fetch(String subject) {
        String url = system.settings().config().getString("schema.registry.url") + "/subjects/" + subject + "/versions/latest";
        return Http.get(system).singleRequest(HttpRequest.create(url))
                .thenApply(HttpResponse::entity)
                .thenCompose(entity -> entity.toStrict(FiniteDuration.create(5, TimeUnit.SECONDS).toMillis(), system))
                .thenCompose(this::streamBytesIntoString)
                .thenApply(ByteString::utf8String)
                .thenApply(this::readJsonTree)
                .thenApply(responseBody -> responseBody.get("schema").textValue())
                .exceptionally(throwable -> {
                    log.warn("encountered exception when fetching schema string, returning empty string", throwable);
                    return "";
                });
    }

    private CompletionStage<ByteString> streamBytesIntoString(akka.http.javadsl.model.HttpEntity.Strict entity) {
        return entity.getDataBytes().runFold(
                ByteString.emptyByteString(),
                ByteString::concat,
                system
        );
    }

    @SneakyThrows
    private JsonNode readJsonTree(String s) {
        return mapper.readTree(s);
    }

}
