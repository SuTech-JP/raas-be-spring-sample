package jp.co.sutech.raas.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.sutech.raas.RaasClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LayoutControllerTest {

    private final RaasClient raasClient = mock(RaasClient.class);
    private final LayoutController controller = new LayoutController(raasClient);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void RaaSのレイアウト一覧APIを呼び結果をそのまま返す() throws URISyntaxException, JsonProcessingException {
        JsonNode layouts = mapper.readTree("[{\"id\":1,\"name\":\"請求書A\"},{\"id\":2,\"name\":\"請求書B\"}]");
        when(raasClient.get(eq("/report/layouts/myapp/myschema"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(layouts));

        JsonNode result = controller.loadLayouts("myapp", "myschema");

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).path("id").asInt());
        assertEquals("請求書B", result.get(1).path("name").asText());
    }

    @Test
    void レイアウトが1件も無い場合は空の配列を返す() throws URISyntaxException, JsonProcessingException {
        when(raasClient.get(eq("/report/layouts/myapp/empty"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(mapper.readTree("[]")));

        JsonNode result = controller.loadLayouts("myapp", "empty");

        assertEquals(0, result.size());
    }

    @Test
    void RaaSが4xxを返した場合は例外をそのまま投げる() throws URISyntaxException {
        when(raasClient.get(eq("/report/layouts/myapp/forbidden"), eq(JsonNode.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, new byte[0], null));

        HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
                () -> controller.loadLayouts("myapp", "forbidden"));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }
}
