package net.funkenburg.gc.backend;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.geo.Tile;
import net.funkenburg.gc.backend.groundspeak.GeocacheType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Builder
@Data
@Slf4j
public class OngoingRequest {
    private final String id = NanoIdUtils.randomNanoId();
    private final Set<Tile> tiles;
    private final Map<String, byte[]> result = new HashMap<>();
    @Builder.Default private String status = "created";

    public void setStatus(String status) {
        log.info("{}: {} -> {}", id, this.status, status);
        this.status = status;
    }

    public void addResult(GeocacheType type, byte[] gpi) {
        result.put(type.name().toLowerCase(Locale.ROOT), gpi);
    }
}
