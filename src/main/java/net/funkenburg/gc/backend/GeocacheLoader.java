package net.funkenburg.gc.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocacheLoader {
    public static final int BATCH_SIZE = 50;
    private final RestTemplate restTemplate;
    private final GroundspeakAccessTokenProvider accessTokenProvider;
    private final ObjectMapper objectMapper;

    public Map<String, String> fetch(String[] gcCodes) throws JsonProcessingException {
        var result = new HashMap<String, String>();
        for (int i = 0; i < gcCodes.length; i += BATCH_SIZE) {
            String[] chunk =
                    Arrays.copyOfRange(gcCodes, i, Math.min(i + BATCH_SIZE, gcCodes.length));
            var chunkResult = fetchBatch(chunk);
            result.putAll(chunkResult);
        }
        for (String gcCode : gcCodes) {
            result.computeIfAbsent(gcCode, this::createHackyPremium);
        }
        return result;
    }

    private String createHackyPremium(String gcCode) {
        try {
            log.debug("Hack premium geocache: {}", gcCode);
            return objectMapper.writeValueAsString(
                    Geocache.builder().code(gcCode).isPremium(true).build());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public Map<String, String> fetchBatch(String[] gcCodes) throws JsonProcessingException {
        var headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        var requestBody =
                RequestBody.builder()
                        .accessToken(accessTokenProvider.get())
                        .cacheCode(RequestBody.CacheCode.builder().cacheCodes(gcCodes).build())
                        .build();
        var entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        var url =
                "https://api.groundspeak.com/LiveV6/Geocaching.svc/internal/SearchForGeocaches?format=json";
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, ResponseBody.class);
        log.info(
                "fetch: {} - {}",
                response.getStatusCode(),
                response.getBody().getStatus().getStatusCode());
        return response.getBody().getRawResponse();
    }

    @Builder
    @Getter
    private static class RequestBody {
        @JsonProperty("AccessToken")
        private String accessToken;

        @JsonProperty("CacheCode")
        private CacheCode cacheCode;

        @JsonProperty("GeocacheLogCount")
        @Builder.Default
        private int geocacheLogCount = 5;

        @JsonProperty("IsLite")
        @Builder.Default
        private boolean isLite = false;

        @JsonProperty("MaxPerPage")
        @Builder.Default
        private int maxPerPage = BATCH_SIZE;

        @JsonProperty("TrackableLogCount")
        @Builder.Default
        private int trackableLogCount = 0;

        @Builder
        @Getter
        private static class CacheCode {
            @JsonProperty("CacheCodes")
            private String[] cacheCodes;
        }
    }

    @Data
    private static class ResponseBody {
        @JsonProperty("Status")
        private StatusBody status;

        @JsonProperty("Geocaches")
        private HashMap<String, Object>[] geocaches;

        @JsonIgnore
        public Map<String, String> getRawResponse() {
            if (geocaches == null) {
                return Collections.emptyMap();
            }
            try {
                var result = new HashMap<String, String>(geocaches.length);
                var mapper = new ObjectMapper();
                for (var geocache : geocaches) {
                    var gcCode = (String) geocache.get("Code");
                    result.put(gcCode, mapper.writeValueAsString(geocache));
                }
                return result;
            } catch (JsonProcessingException e) {
                log.error("Error formatting raw response", e);
                return Collections.emptyMap();
            }
        }

        @Data
        private static class StatusBody {
            @JsonProperty("StatusCode")
            private int statusCode;
        }
    }
}
