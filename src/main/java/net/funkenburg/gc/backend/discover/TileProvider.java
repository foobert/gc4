package net.funkenburg.gc.backend.discover;

import net.funkenburg.gc.backend.geo.Tile;

import java.util.Set;

public interface TileProvider {
    /** Returns all geocache codes of a tile */
    Set<String> getGcCodes(Tile tile);
}
