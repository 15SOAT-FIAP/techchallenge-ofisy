package br.com.ofisy.adapters.tracing;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import tools.jackson.databind.json.JsonMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTraceContextReaderTest {

    private static final String TRACEPARENT = "00-6af79dd30000000039a72b7697c5509c-63cc4ef521ebfd0c-01";
    private static final String ATTRIBUTE_JSON = "{\"traceparent\":\"%s\",\"x-datadog-trace-id\":\"4154336969624998044\"}".formatted(TRACEPARENT);
    private static final Map<String, String> EXPECTED = Map.of(
            "traceparent", TRACEPARENT,
            "x-datadog-trace-id", "4154336969624998044");

    private final MessageTraceContextReader reader = new MessageTraceContextReader(JsonMapper.builder().build());

    @Test
    void shouldReadBinaryAttributeDeliveredAsSdkBytes() {
        assertThat(reader.read(SdkBytes.fromUtf8String(ATTRIBUTE_JSON))).isEqualTo(EXPECTED);
    }

    @Test
    void shouldReadAttributeDeliveredAsText() {
        assertThat(reader.read(ATTRIBUTE_JSON)).isEqualTo(EXPECTED);
    }

    @Test
    void shouldReadAttributeDeliveredAsBytes() {
        assertThat(reader.read(ATTRIBUTE_JSON.getBytes(StandardCharsets.UTF_8))).isEqualTo(EXPECTED);
        assertThat(reader.read(ByteBuffer.wrap(ATTRIBUTE_JSON.getBytes(StandardCharsets.UTF_8)))).isEqualTo(EXPECTED);
    }

    @Test
    void shouldReturnEmptyWhenAttributeIsMissing() {
        assertThat(reader.read(null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenAttributeIsUnreadable() {
        assertThat(reader.read("not-json")).isEmpty();
        assertThat(reader.read(SdkBytes.fromUtf8String("eyJ0cmFjZXBhcmVudCI6IjAwIn0="))).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenAttributeHasUnexpectedType() {
        assertThat(reader.read(42)).isEmpty();
    }
}
