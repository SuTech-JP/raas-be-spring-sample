package jp.co.sutech.raas.sample;

import com.fasterxml.jackson.databind.JsonNode;
import jp.co.sutech.raas.RaasClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

/**
 * raas-be-play-sample の loadLayouts 相当。
 * レイアウト一覧を取得する。
 */
@RestController
@RequestMapping("/raas/report/layout")
public class LayoutController {
    private final RaasClient raasClient;

    public LayoutController(RaasClient raasClient) {
        this.raasClient = raasClient;
    }

    @GetMapping("/{application}/{schema}")
    public JsonNode loadLayouts(@PathVariable String application, @PathVariable String schema) throws URISyntaxException {
        return raasClient.get("/report/layouts/" + application + "/" + schema, JsonNode.class).getBody();
    }
}
