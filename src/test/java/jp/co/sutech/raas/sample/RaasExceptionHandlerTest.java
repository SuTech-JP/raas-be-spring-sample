package jp.co.sutech.raas.sample;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaasExceptionHandlerTest {

    private final RaasExceptionHandler handler = new RaasExceptionHandler();

    @Test
    void RaaS由来の4xxはそのままのstatusで返す() {
        HttpClientErrorException e = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        ResponseEntity<?> response = handler.handleRaasClientError(e);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void リトライ枯渇など想定外の例外は500になる() {
        ResourceAccessException e = new ResourceAccessException("connection refused");

        ResponseEntity<?> response = handler.handleUnexpectedError(e);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void 不正なリクエストボディは400になる() {
        HttpMessageNotReadableException e =
                new HttpMessageNotReadableException("broken json", new IOException("broken json"), null);

        ResponseEntity<?> response = handler.handleBadRequest(e);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid request body"));
    }
}
