package net.funkenburg.gc.backend.gpx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.groundspeak.Geocache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
public class GpxBuilder implements AutoCloseable {
    private final ByteArrayOutputStream stream;
    private final OutputStreamWriter writer;

    private int count;

    public GpxBuilder() throws IOException {
        this.stream = new ByteArrayOutputStream();
        this.writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        header();
    }

    private void header() throws IOException {
        writer.write(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-\n"
                        + "instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" version=\"1.1\" creator=\"cachecache\">");
    }

    public void add(Geocache geocache) throws IOException {
        //        log.info(
        //                "GPX: {} {}/{}",
        //                geocache.getCode(),
        //                geocache.getDifficulty(),
        //                geocache.getTerrain());
        var clean = new CleanView(geocache);

        writer.write(
                "<wpt lat=\""
                        + geocache.getLatitude()
                        + "\" lon=\""
                        + geocache.getLongitude()
                        + "\">");
        writer.write("<name>" + clean.title() + "</name>");
        writer.write("<desc>" + clean.description() + "</desc>");
        writer.write("<cmt>" + clean.description() + "</cmt>");
        writer.write("<type>Geocache</type>");
        writer.write("</wpt>");
        writer.write("\n");

        count++;
    }

    @Override
    public void close() throws Exception {
        writer.write("</gpx>");
        writer.close();
    }

    public String getOutput() {
        return stream.toString(StandardCharsets.UTF_8);
    }

    public int getCount() {
        return count;
    }

    @RequiredArgsConstructor
    private static class CleanView {
        private final Geocache geocache;

        public String title() {
            return code() + " " + size() + type() + " " + skill();
        }

        private String code() {
            return geocache.getCode().substring(2);
        }

        private String type() {
            return geocache.getGeocacheType().name().substring(0, 1).toUpperCase(Locale.ENGLISH);
        }

        private String skill() {
            return geocache.getDifficulty() + "/" + geocache.getTerrain();
        }

        private String size() {
            return geocache.getContainerType()
                    .getContainerTypeName()
                    .substring(0, 1)
                    .toUpperCase(Locale.ENGLISH);
        }

        private String hint() {
            return clean(geocache.getHint());
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
                    .replaceAll(" {2,}", " ")
                    .replaceAll("[^a-zA-Z0-9;:?!,.-=_/@$%*+()<> |\n]", "")
                    .trim();
        }

        public String description() {
            var hint = hint();
            var description = code() + " " + name() + (hint.length() > 0 ? "\n" : "") + hint;
            return description.substring(0, Math.min(100, description.length()));
        }

        private String name() {
            return clean(geocache.getName());
        }
    }
}
