package net.funkenburg.gc.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocacheRepository {
    private final GeocacheLoader loader;

    private final RawGeocacheRepository rawGeocacheRepository;

    public Set<RawGeocache> lookupGeocaches(Collection<String> gcCodes) {
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

        try {
            String[] codes = needFetch.toArray(String[]::new);
            Map<String, String> raw = loader.fetch(codes);
            for (var entry : raw.entrySet()) {
                rawGeocacheRepository.update(entry.getKey(), entry.getValue(), timestamp);
                var rawGeocache = new RawGeocache();
                rawGeocache.setRaw(entry.getValue());
                rawGeocache.setId(entry.getKey());
                rawGeocache.setTimestamp(timestamp);
                result.add(rawGeocache);
            }
        } catch (JsonProcessingException e) {
            log.error("lookupGeocaches", e);
            return Collections.emptySet();
        }
        return result;
    }

    private Instant getTimestamp(String gcCode) {
        return rawGeocacheRepository
                .findById(gcCode)
                .map(RawGeocache::getTimestamp)
                .orElse(Instant.MIN);
    }

    private Optional<RawGeocache> loadFromDb(String gcCode) {
        return rawGeocacheRepository.findById(gcCode);
    }
}
