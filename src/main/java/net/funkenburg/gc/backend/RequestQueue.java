package net.funkenburg.gc.backend;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.discover.TileProvider;
import net.funkenburg.gc.backend.fetch.GeocacheProvider;
import net.funkenburg.gc.backend.fetch.RawGeocache;
import net.funkenburg.gc.backend.geo.Tile;
import net.funkenburg.gc.backend.gpx.GpiBuilder;
import net.funkenburg.gc.backend.gpx.GpxBuilder;
import net.funkenburg.gc.backend.groundspeak.Geocache;
import net.funkenburg.gc.backend.groundspeak.GeocacheType;
import net.funkenburg.gc.backend.groundspeak.Parser;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestQueue {
    private final ExecutorService executorService;

    private final TileProvider tiles;
    private final GeocacheProvider geocaches;
    private final Parser parser;
    private final GpiBuilder gpiBuilder;

    private final ConcurrentMap<String, OngoingRequest> requests = new ConcurrentHashMap<>();
    private final DelayQueue<DelayedRequestId> timeout = new DelayQueue<>();

    public Collection<String> getIds() {
        return requests.keySet();
    }

    @Data
    private static class DelayedRequestId implements Delayed {
        private final String requestId;
        private final Instant cutoff;

        private DelayedRequestId(String requestId, Instant cutoff) {
            this.requestId = requestId;
            this.cutoff = cutoff;
        }

        public static DelayedRequestId of(OngoingRequest request) {
            return new DelayedRequestId(request.getId(), Instant.now().plus(1, ChronoUnit.HOURS));
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return Duration.between(Instant.now(), cutoff).get(ChronoUnit.SECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }
    }

    public void enqueue(OngoingRequest request) {
        cleanup();
        timeout.add(DelayedRequestId.of(request));
        requests.put(request.getId(), request);
        request.setDetail("queued");
        this.executorService.submit(
                () -> {
                    try {
                        request.setState(RequestState.PROCESSING);
                        request.setDetail("discover");
                        var gcCodes = new HashSet<String>();
                        for (Tile tile : request.getTiles()) {
                            gcCodes.addAll(tiles.getGcCodes(tile));
                        }

                        request.setDetail("fetch");
                        var rawGeocaches = geocaches.getRawGeocaches(gcCodes);

                        request.setDetail("gpx");
                        var interestingTypes =
                                List.of(
                                        GeocacheType.TRADITIONAL,
                                        GeocacheType.MULTI,
                                        GeocacheType.EARTH);
                        Map<GeocacheType, GpxBuilder> builders = new HashMap<>();
                        for (var type : interestingTypes) {
                            builders.put(type, new GpxBuilder());
                        }

                        for (RawGeocache raw : rawGeocaches) {
                            Geocache geocache = parser.parse(raw.getRawString());
                            if (geocache.isPremium()
                                    || geocache.isArchived()
                                    || geocache.isLocked()
                                    || !geocache.isPublished()) {
                                log.debug("Skip {}", geocache.getCode());
                                continue;
                            }
                            GpxBuilder gpxBuilder = builders.get(geocache.getGeocacheType());
                            if (gpxBuilder != null) {
                                gpxBuilder.add(geocache);
                            }
                        }

                        request.setDetail("gpi");
                        for (var gpx : builders.entrySet()) {
                            gpx.getValue().close();
                            byte[] gpi =
                                    gpiBuilder.convert(gpx.getValue().getOutput(), gpx.getKey());
                            request.addResult(gpx.getKey(), gpi);
                        }

                        request.setDetail("done");
                        request.setState(RequestState.DONE);
                    } catch (Exception e) {
                        log.error("Error processing queue", e);
                    }
                });
    }

    private void cleanup() {
        DelayedRequestId id;
        while ((id = timeout.poll()) != null) {
            log.info("Timeout {}", id.requestId);
            requests.remove(id.requestId);
        }
    }

    public Optional<OngoingRequest> lookup(String id) {
        return Optional.ofNullable(requests.get(id));
    }
}
