package net.funkenburg.gc.backend.gpx;

import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.groundspeak.Geocache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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
        var clean = new StringCleaner(geocache);

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
}
