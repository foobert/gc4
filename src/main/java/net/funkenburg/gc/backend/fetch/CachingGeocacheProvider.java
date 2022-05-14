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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class CachingGeocacheProvider implements GeocacheProvider {
    private final GeocacheProvider delegate;

    private final RawGeocacheRepository repo;

    @Override
    public Set<RawGeocache> getRawGeocaches(Collection<String> gcCodes) {
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

        Set<RawGeocache> rawGeocaches = delegate.getRawGeocaches(needFetch);
        for (var entry : rawGeocaches) {
            repo.update(entry.getId(), entry.getRawString(), entry.getTimestamp());
            result.add(entry);
        }
        return result;
    }

    private Instant getTimestamp(String gcCode) {
        return repo.findById(gcCode).map(RawGeocache::getTimestamp).orElse(Instant.MIN);
    }

    private Optional<RawGeocache> loadFromDb(String gcCode) {
        return repo.findById(gcCode);
    }
}
