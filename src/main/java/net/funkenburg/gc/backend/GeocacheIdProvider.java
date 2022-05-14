package net.funkenburg.gc.backend;

import java.util.Set;

@FunctionalInterface
public interface GeocacheIdProvider {
    Set<String> fetchTile(Tile tile);
}
