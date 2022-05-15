package net.funkenburg.gc.backend.discover;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("cache")
@Primary
public class CachingTileProvider implements TileProvider {

    private final TileProvider delegate;

    private final TileRepository repo;

    @Override
    public Set<String> getGcCodes(net.funkenburg.gc.backend.geo.Tile tile) {
        var cutoff = Instant.now().minus(48, ChronoUnit.HOURS);
        var ts = getTimestamp(tile);
        if (ts.isBefore(cutoff)) {
            Set<String> strings = delegate.getGcCodes(tile);
            save(tile, strings);
            return strings;
        } else {
            return loadFromDb(tile);
        }
    }

    private Set<String> loadFromDb(net.funkenburg.gc.backend.geo.Tile tile) {
        return repo.findById(tile.getQuadkey())
                .map(Tile::getGcCodes)
                .map(x -> new HashSet<>(Arrays.stream(x).toList()))
                .orElse(new HashSet<>());
    }

    private Instant getTimestamp(net.funkenburg.gc.backend.geo.Tile tile) {
        return repo.findById(tile.getQuadkey()).map(Tile::getTimestamp).orElse(Instant.MIN);
    }

    private void save(net.funkenburg.gc.backend.geo.Tile tile, Set<String> gcCodes) {
        repo.update(tile.getQuadkey(), gcCodes.toArray(String[]::new), Instant.now());
    }
}
