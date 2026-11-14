package br.com.ofisy.adapters.tracing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// Lê o contexto do trace que o dd-java-agent injeta no atributo _datadog da mensagem SNS/SQS.
// O agent não continua esse trace sozinho no @SqsListener, então o listener o restaura com o TraceContextPropagator.
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTraceContextReader {

    public static final String TRACE_CONTEXT_ATTRIBUTE = "_datadog";

    private static final TypeReference<Map<String, String>> TRACE_CONTEXT_TYPE = new TypeReference<>() {};

    private final JsonMapper jsonMapper;

    // O agent publica o atributo como Binary, que o Spring Cloud AWS entrega como SdkBytes; texto e bytes também são aceitos.
    // Um atributo ausente ou ilegível não pode impedir o consumo: o evento é processado sem a ligação com o trace de origem
    public Map<String, String> read(Object attributeValue) {
        String json = asString(attributeValue);
        if (json == null || json.isBlank()) {
            return Map.of();
        }

        try {
            return jsonMapper.readValue(json, TRACE_CONTEXT_TYPE);
        } catch (JacksonException e) {
            log.warn("MessageTraceContextReader.read >> ignoring unreadable {} message attribute", TRACE_CONTEXT_ATTRIBUTE, e);
            return Map.of();
        }
    }

    private String asString(Object attributeValue) {
        return switch (attributeValue) {
            case null -> null;
            case SdkBytes sdkBytes -> sdkBytes.asUtf8String();
            case String text -> text;
            case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
            case ByteBuffer buffer -> StandardCharsets.UTF_8.decode(buffer.duplicate()).toString();
            default -> {
                log.warn("MessageTraceContextReader.asString >> ignoring {} message attribute of type {}",
                        TRACE_CONTEXT_ATTRIBUTE, attributeValue.getClass().getName());
                yield null;
            }
        };
    }
}
