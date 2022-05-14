package net.funkenburg.gc.backend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestProcessor {

    //    private final TileRepository tileRepository;

    public OngoingRequest request(Request request) {
        // 1. POST /gpx with tiles or coordinate center
        // 2. Accept request, compute tiles (if not given), enqueue request
        // 3. Loop over requests queue A, compute tile -> GC codes
        // 4. Loop over requests queue B, compute GC codes -> fetch
        // 5. Loop over requests queue C, compute GPX
        // 6. GET /gpx, either 204(?) or 200 with GPX content
        // 7. Loop over requests, reap dead items

        var center = new Coordinate(request.getLatitude(), request.getLongitude());
        var tiles = Tile.near(center, request.getDistance());

        //        var geocaches =
        // Flux.fromIterable(tiles).flatMap(tileRepository::lookup).collectList();

        throw new UnsupportedOperationException();
    }
}
