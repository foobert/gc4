package net.funkenburg.gc.backend;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class Request {
    private final double latitude;
    private final double longitude;

    public double getDistance() {
        // hard-coded for now
        return 10_000;
    }
}
