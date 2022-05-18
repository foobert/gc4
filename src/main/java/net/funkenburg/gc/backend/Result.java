package net.funkenburg.gc.backend;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    private final byte[] gpi;
    private final String gpx;
    private final int count;
}
