package net.funkenburg.gc.backend.groundspeak;

import java.util.HashMap;
import java.util.Map;

public enum GeocacheType {
    TRADITIONAL(2),
    WHEREIGO(1858),
    EVENT(6),
    MYSTERY(8),
    MULTI(3),
    EARTH(137),
    VIRTUAL(4),
    LETTERBOX(5),
    CITO(13),
    APE(9),
    WEBCAM(11),
    MEGAEVENT(453),
    GPSADVENTURES(1304),
    GCHQ(3773),
    GIGAEVENT(7005);

    public final int value;

    GeocacheType(int value) {
        this.value = value;
    }

    private static final Map<Integer, GeocacheType> BY_VALUE = new HashMap<>();

    static {
        for (var e : values()) {
            BY_VALUE.put(e.value, e);
        }
    }

    public static GeocacheType of(int value) {
        return BY_VALUE.get(value);
    }
}
