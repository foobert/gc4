package net.funkenburg.gc.backend.geo;

import org.junit.jupiter.api.Test;

class TileTest {
    @Test
    void testFoo() {
        var topLeft = Tile.fromCoordinates(new Coordinate(39.9880353137982, 2.2585241721318727));
        var bottomRight =
                Tile.fromCoordinates(new Coordinate(39.23773940219697, 3.5091854391187147));

        System.out.println(topLeft);
        System.out.println(bottomRight);
    }
}
