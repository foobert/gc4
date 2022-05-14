package net.funkenburg.gc.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.web.bind.annotation.RestController
public class RestController {

    private final TileRepository tileRepo;
    private final GroundspeakAccessTokenProvider accessTokenProvider;
    private final GeocacheLoader loader;
    private final RequestQueue requestQueue;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String index() {
        String accessToken = accessTokenProvider.get();
        log.info("token: {}", accessToken);
        Coordinate fake = new Coordinate(51.33702374516727, 12.372297128375628);
        var tile = Tile.fromCoordinates(fake);
        OngoingRequest request = OngoingRequest.builder().tiles(Set.of(tile)).build();
        requestQueue.enqueue(request);
        return request.getId();
    }

    @GetMapping("/status")
    public String status(@RequestParam String id) {
        return requestQueue.lookup(id).map(OngoingRequest::getStatus).orElse("not found");
    }

    @GetMapping(value = "/gpi", produces = "xml/gpx")
    public String output(@RequestParam String id) {
        return "TODO";
        //        return requestQueue.lookup(id).map(OngoingRequest::getOutput).orElse("");
    }
}
