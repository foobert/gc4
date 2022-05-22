package net.funkenburg.gc.backend.gpx;

import lombok.RequiredArgsConstructor;
import net.funkenburg.gc.backend.groundspeak.Geocache;

import java.util.Locale;

@RequiredArgsConstructor
class StringCleaner {
    private final Geocache geocache;

    public String title() {
        return code() + " " + size() + type() + " " + skill();
    }

    String code() {
        return geocache.getCode().substring(2);
    }

    String type() {
        return geocache.getGeocacheType().name().substring(0, 1).toUpperCase(Locale.ENGLISH);
    }

    String skill() {
        return geocache.getDifficulty() + "/" + geocache.getTerrain();
    }

    String size() {
        return geocache.getContainerType()
                .getContainerTypeName()
                .substring(0, 1)
                .toUpperCase(Locale.ENGLISH);
    }

    String hint() {
        return clean(geocache.getEncodedHints());
    }

    private String clean(String data) {
        if (data == null) {
            return "";
        }
        return data.replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
                .replaceAll("Ä", "AE")
                .replaceAll("Ö", "OE")
                .replaceAll("Ü", "UE")
                .replaceAll("ß", "ss")
                .replaceAll("<\\w+/?>", " ")
                .replaceAll(" {2,}", " ")
                .replaceAll("[^\\w;:?!,.\\-=_/@$%*+() |\n]", "")
                .trim();
    }

    public String description() {
        var hint = hint();
        var description = code() + " " + name() + (hint.length() > 0 ? "\n" : "") + hint;
        return description.substring(0, Math.min(100, description.length()));
    }

    String name() {
        return clean(geocache.getName());
    }
}
