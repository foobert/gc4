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
    private final Map<String, Result> results = new HashMap<>();

    @Builder.Default private String detail = "created";
    @Builder.Default private RequestState state = RequestState.CREATED;

    public void setDetail(String detail) {
        log.info("{}: {} -> {}", id, this.detail, detail);
        this.detail = detail;
    }

    public void addResult(GeocacheType type, Result result) {
        results.put(type.name().toLowerCase(Locale.ROOT), result);
    }
}
