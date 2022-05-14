package net.funkenburg.gc.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginBasedAccessTokenProvider implements GroundspeakAccessTokenProvider {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groundspeak.consumer_key}")
    private String consumerKey;

    @Value("${groundspeak.username}")
    private String username;

    @Value("${groundspeak.password}")
    private String password;

    @Value("${groundspeak.access_token:}")
    private String accessToken;

    @Override
    @Synchronized
    public String get() {
        if (accessToken == null || accessToken.isBlank()) {
            try {
                accessToken = login();
            } catch (IOException e) {
                log.error("Unable to fetch token", e);
            }
        }
        return accessToken;
    }

    private String login() throws IOException {
        var headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        var requestBody =
                RequestBody.builder()
                        .consumerKey(consumerKey)
                        .username(username)
                        .password(password)
                        .build();
        var entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        var url = "https://api.groundspeak.com/LiveV6/Geocaching.svc/internal/Login?format=json";
        //            url = "http://localhost:8081";
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, ResponseBody.class);
        log.info("Login: {}", response.getStatusCode());
        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null
                || response.getBody().getAccessToken() == null) {
            throw new IOException("Got invalid response from login");
        }
        log.info("Login Body: {}", response.getBody());
        log.info("Access token: {}", response.getBody().getAccessToken());
        return response.getBody().getAccessToken();
    }

    @Builder
    @Getter
    private static class RequestBody {
        @JsonProperty("ConsumerKey")
        private final String consumerKey;

        @JsonProperty("UserName")
        private final String username;

        @JsonProperty("Password")
        private final String password;
    }

    @Data
    private static class ResponseBody {
        @JsonProperty("GroundspeakAccessToken")
        private String accessToken;
    }
}
