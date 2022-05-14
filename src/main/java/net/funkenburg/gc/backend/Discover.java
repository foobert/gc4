package net.funkenburg.gc.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Discover implements GeocacheIdProvider {

    private final RestTemplate restTemplate;
    private final DiscoverUrlProvider urlProvider;

    @Override
    public Set<String> fetchTile(Tile tile) {
        log.info("Loading GC codes for {}", tile);
        String baseUrl = urlProvider.get();
        fetchTilePng(tile, baseUrl);
        return fetchTileInfo(tile, baseUrl);
    }

    private void fetchTilePng(Tile tile, String baseUrl) {
        var headers = new HttpHeaders();
        headers.set("Accept", MediaType.ALL_VALUE);
        headers.set(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36");
        var entity = new HttpEntity<ResponseBody>(headers);

        var url = baseUrl + "/map.png?x={x}&y={y}&z={z}";

        var forEntity =
                restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class, tile.x(), tile.y(), tile.z());
        log.info("map.png: {} -> {}", url, forEntity.getStatusCode());
    }

    private Set<String> fetchTileInfo(Tile tile, String baseUrl) {
        var headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36");
        var entity = new HttpEntity<ResponseBody>(headers);

        var url = baseUrl + "/map.info?x={x}&y={y}&z={z}";

        ResponseEntity<ResponseBody> forEntity =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        ResponseBody.class,
                        tile.x(),
                        tile.y(),
                        tile.z());
        log.info("map.info: {} -> {}", url, forEntity.getStatusCode());
        if (forEntity.hasBody()) {
            return forEntity.getBody().getGeocacheCodes();
        } else {
            return Collections.emptySet();
        }
    }

    public static class ResponseBody {
        public Map<String, List<ResponseObject>> data;

        static class ResponseObject {
            public String i;
        }

        @JsonIgnore
        public Set<String> getGeocacheCodes() {
            if (data == null) {
                return Collections.emptySet();
            }
            return data.values().stream()
                    .flatMap(v -> v.stream().map(o -> o.i))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }
}
