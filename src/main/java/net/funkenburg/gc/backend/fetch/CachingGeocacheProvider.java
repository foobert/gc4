package net.funkenburg.gc.backend.fetch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class CachingGeocacheProvider implements GeocacheProvider {
    private final GeocacheProvider delegate;

    private final RawGeocacheRepository repo;

    @Override
    public Stream<RawGeocache> getRawGeocaches(Collection<String> gcCodes) {
        var result = new HashSet<RawGeocache>(gcCodes.size());
        var timestamp = Instant.now();
        var cutoff = timestamp.minus(48, ChronoUnit.HOURS);
        var grouped =
                gcCodes.stream()
                        .collect(Collectors.groupingBy(x -> getTimestamp(x).isBefore(cutoff)));
        var needFetch = grouped.getOrDefault(true, Collections.emptyList());
        var loadFromDb = grouped.getOrDefault(false, Collections.emptyList());

        log.info(
                "Fetching {} geocaches, {} from DB and {} from Groundspeak",
                gcCodes.size(),
                loadFromDb.size(),
                needFetch.size());

        for (var gcCode : loadFromDb) {
            var rawGeocache = loadFromDb(gcCode);
            if (rawGeocache.isPresent()) {
                result.add(rawGeocache.get());
            } else {
                log.info("Tried to load {} from DB, but wasn't there", gcCode);
                needFetch.add(gcCode);
            }
        }

        Stream<RawGeocache> rawGeocaches = delegate.getRawGeocaches(needFetch);
        rawGeocaches.forEach(
                entry -> {
                    log.info("Updated {}", entry.getId());
                    repo.update(entry.getId(), entry.getRawString(), entry.getTimestamp());
                    result.add(entry);
                });
        return result.stream();
    }

    private Instant getTimestamp(String gcCode) {
        return repo.findById(gcCode).map(RawGeocache::getTimestamp).orElse(Instant.MIN);
    }

    private Optional<RawGeocache> loadFromDb(String gcCode) {
        return repo.findById(gcCode);
    }
}
