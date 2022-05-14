package net.funkenburg.gc.backend;

import java.util.ArrayList;
import java.util.List;

public record Tile(int x, int y, int z) {
    private static final int DEFAULT_ZOOM = 12;

    public String getQuadkeyString() {
        StringBuilder quadKey = new StringBuilder();
        for (int i = z; i > 0; i--) {
            int digit = 0;
            var mask = i << (i - 1);
            if ((x & mask) != 0) {
                digit++;
            }
            if ((y & mask) != 0) {
                digit += 2;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();
    }

    public int getQuadkey() {
        int result = 0;
        for (int i = 0; i < z; i++) {
            result |= (x & 1 << i) << i | (y & 1 << i) << (i + 1);
        }
        return result;
    }

    public static Tile fromCoordinates(Coordinate coordinate) {
        return fromCoordinates(coordinate, DEFAULT_ZOOM);
    }

    public static Tile fromCoordinates(Coordinate coordinate, int zoom) {
        double lat = coordinate.lat();
        double lon = coordinate.lon();
        double latRad = lat * Math.PI / 180;
        double n = Math.pow(2, zoom);
        int xtile = (int)((lon + 180.0) / 360.0 * n);
        int ytile =
                (int)
                        (
                                (1.0 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI)
                                        / 2.0
                                        * n);
        return new Tile(xtile, ytile, zoom);
    }

    public static List<Tile> near(Coordinate coordinate, double radius) {
        // as a first approximation, use a square instead of a circle
        var topLeft = coordinate.move(radius, 315);
        var bottomRight = coordinate.move(radius, 135);

        var topLeftTile = Tile.fromCoordinates(topLeft);
        var bottomRightTile = Tile.fromCoordinates(bottomRight);

        var result = new ArrayList<Tile>();
        for (int x = topLeftTile.x; x <= bottomRightTile.x; x++) {
            for (int y = topLeftTile.y; y <= bottomRightTile.y; y++) {
                result.add(new Tile(x, y, topLeftTile.z));
            }
        }

        return result;
    }
}
