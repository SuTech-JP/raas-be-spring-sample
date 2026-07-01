package jp.co.sutech.raas.sample;

import jp.co.sutech.raas.EnableRaasProxy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRaasProxy(baseUrl = "/raas")
public class RaasBeSpringSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(RaasBeSpringSampleApplication.class, args);
    }
}
