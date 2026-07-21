package jp.co.sutech.raas.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jp.co.sutech.raas.RaasClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URISyntaxException;

/**
 * raas-be-play-sample の loadLogData 相当。
 * CSVインポートログを取得し、status が FINISH の場合は詳細データも取得してマージする。
 */
@RestController
@RequestMapping("/raas/report/result")
public class ImportLogController {
    private static final String FINISH_STATUS = "FINISH";

    private final RaasClient raasClient;

    public ImportLogController(RaasClient raasClient) {
        this.raasClient = raasClient;
    }

    @GetMapping("/{targetId}")
    public JsonNode loadLogData(@PathVariable String targetId) throws URISyntaxException {
        JsonNode log = raasClient.get("/datatraveler/import/logs/" + targetId, JsonNode.class).getBody();
        if (log == null) {
            throw HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
        }

        if (!FINISH_STATUS.equals(log.path("status").asText())) {
            return log;
        }

        JsonNode details = raasClient.get("/datatraveler/import/logs/" + targetId + "/data", JsonNode.class).getBody();
        ObjectNode merged = ((ObjectNode) log).deepCopy();
        merged.set("details", details);
        return merged;
    }
}
