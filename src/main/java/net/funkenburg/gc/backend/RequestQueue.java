package net.funkenburg.gc.backend;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.discover.TileProvider;
import net.funkenburg.gc.backend.fetch.GeocacheProvider;
import net.funkenburg.gc.backend.geo.Tile;
import net.funkenburg.gc.backend.gpx.GpiBuilder;
import net.funkenburg.gc.backend.gpx.GpxBuilder;
import net.funkenburg.gc.backend.groundspeak.GeocacheType;
import net.funkenburg.gc.backend.groundspeak.Parser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
                        AtomicInteger tileProgress = new AtomicInteger(0);
                        Set<Tile> tiles = request.getTiles();
                        for (Tile tile : tiles) {
                            request.progress("tiles", tileProgress.incrementAndGet(), tiles.size());
                            gcCodes.addAll(this.tiles.getGcCodes(tile));
                        }

                        request.setDetail("fetch");
                        AtomicInteger fetchProgress = new AtomicInteger();
                        var rawGeocaches =
                                observe(
                                        geocaches.getRawGeocaches(gcCodes),
                                        raw -> {
                                            request.progress(
                                                    "fetch",
                                                    fetchProgress.incrementAndGet(),
                                                    gcCodes.size());
                                        });

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

                        rawGeocaches
                                .map(raw -> (parser).parse(raw.getRawString()))
                                .filter(
                                        geocache ->
                                                !geocache.isPremium()
                                                        && !geocache.isArchived()
                                                        && !geocache.isLocked()
                                                        && geocache.isPublished())
                                .forEach(
                                        geocache -> {
                                            GpxBuilder gpxBuilder =
                                                    builders.get(geocache.getGeocacheType());
                                            if (gpxBuilder != null) {
                                                try {
                                                    gpxBuilder.add(geocache);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        });

                        request.setDetail("gpi");
                        for (var gpx : builders.entrySet()) {
                            gpx.getValue().close();
                            byte[] gpi =
                                    gpiBuilder.convert(gpx.getValue().getOutput(), gpx.getKey());
                            var result =
                                    Result.builder()
                                            .gpi(gpi)
                                            .gpx(gpx.getValue().getOutput())
                                            .count(gpx.getValue().getCount())
                                            .build();
                            request.addResult(gpx.getKey(), result);
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

    private <T> Stream<T> observe(Stream<T> stream, Consumer<T> observer) {
        Iterator<T> iterator = stream.iterator();
        Iterator<T> resultIterator =
                new Iterator<>() {
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public T next() {
                        T next = iterator.next();
                        observer.accept(next);
                        return next;
                    }
                };
        return StreamSupport.stream(((Iterable<T>) () -> resultIterator).spliterator(), false);
    }

    public Optional<OngoingRequest> lookup(String id) {
        return Optional.ofNullable(requests.get(id));
    }
}
