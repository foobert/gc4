package net.funkenburg.gc.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TileRepository {

    private final GeocacheIdProvider discover;

    private final GeocacheIdRepository repo;

    public Collection<String> lookupGeocacheIds(Tile tile) {
        var cutoff = Instant.now().minusSeconds(300);
        var ts = getTimestamp(tile);
        if (false && ts.isBefore(cutoff)) {
            Set<String> strings = discover.fetchTile(tile);
            save(tile, strings);
            return strings;
        } else {
            return loadFromDb(tile);
        }
    }

    private Set<String> loadFromDb(Tile tile) {
        return repo.findById(tile.getQuadkey())
                .map(GeocacheId::getGeocacheIds)
                .map(x -> new HashSet<>(Arrays.stream(x).toList()))
                .orElse(new HashSet<>());
    }

    private Instant getTimestamp(Tile tile) {
        return repo.findById(tile.getQuadkey()).map(GeocacheId::getTimestamp).orElse(Instant.MIN);
    }

    private void save(Tile tile, Set<String> gcCodes) {
        repo.update(tile.getQuadkey(), gcCodes.toArray(String[]::new), Instant.now());
    }
}
