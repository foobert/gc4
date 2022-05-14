package net.funkenburg.gc.backend.geo;

public record Coordinate(double lat, double lon) {
    private static final int EARTH_RADIUS = 6371000; // radius of earth in meters

    public Coordinate move(double distance, double bearing) {
        // see http://www.movable-type.co.uk/scripts/latlong.html

        // (all angles in radians)
        double latRad = this.lat * Math.PI / 180;
        double lonRad = this.lon * Math.PI / 180;
        double bearingRad = bearing * Math.PI / 180;

        double latRad2 =
                Math.asin(
                        Math.sin(latRad) * Math.cos(distance / EARTH_RADIUS)
                                + Math.cos(latRad)
                                        * Math.sin(distance / EARTH_RADIUS)
                                        * Math.cos(bearingRad));
        double lonRad2 =
                lonRad
                        + Math.atan2(
                                Math.sin(bearingRad)
                                        * Math.sin(distance / EARTH_RADIUS)
                                        * Math.cos(latRad),
                                Math.cos(distance / EARTH_RADIUS)
                                        - Math.sin(latRad) * Math.sin(latRad2));

        // The longitude can be normalised to −180…+180 using (lon+540)%360-180
        lonRad2 = (lonRad2 + 540) % 360 - 180;

        double lat2 = latRad2 * 180 / Math.PI;
        double lon2 = lonRad2 * 180 / Math.PI;
        return new Coordinate(lat2, lon2);
    }
}
