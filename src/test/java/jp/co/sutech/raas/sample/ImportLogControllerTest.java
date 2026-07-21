package jp.co.sutech.raas.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.sutech.raas.RaasClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportLogControllerTest {

    private final RaasClient raasClient = mock(RaasClient.class);
    private final ImportLogController controller = new ImportLogController(raasClient);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void statusがFINISH以外なら詳細取得をせずログをそのまま返す() throws URISyntaxException, JsonProcessingException {
        JsonNode log = mapper.readTree("{\"targetId\":\"t-1\",\"status\":\"RUNNING\"}");
        when(raasClient.get(eq("/datatraveler/import/logs/t-1"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(log));

        JsonNode result = controller.loadLogData("t-1");

        assertEquals("RUNNING", result.path("status").asText());
        assertFalse(result.has("details"));
        verify(raasClient, never()).get(eq("/datatraveler/import/logs/t-1/data"), eq(JsonNode.class));
    }

    @Test
    void statusがFINISHなら詳細データを取得してマージする() throws URISyntaxException, JsonProcessingException {
        JsonNode log = mapper.readTree("{\"targetId\":\"t-2\",\"status\":\"FINISH\"}");
        JsonNode details = mapper.readTree("[{\"row\":1}]");
        when(raasClient.get(eq("/datatraveler/import/logs/t-2"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(log));
        when(raasClient.get(eq("/datatraveler/import/logs/t-2/data"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(details));

        JsonNode result = controller.loadLogData("t-2");

        assertEquals("FINISH", result.path("status").asText());
        assertEquals(1, result.path("details").get(0).path("row").asInt());
    }

    @Test
    void 該当するログが無い場合は404を投げる() throws URISyntaxException {
        when(raasClient.get(eq("/datatraveler/import/logs/missing"), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(null));

        HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
                () -> controller.loadLogData("missing"));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
