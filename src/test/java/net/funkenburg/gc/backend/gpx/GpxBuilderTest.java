package net.funkenburg.gc.backend.gpx;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.funkenburg.gc.backend.groundspeak.Geocache;
import net.funkenburg.gc.backend.groundspeak.Parser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

class GpxBuilderTest {
    private static Geocache realExample;

    @BeforeAll
    static void setupClass() throws IOException {
        Resource is = new DefaultResourceLoader().getResource("gc.json");
        String s = Files.readString(is.getFile().toPath());
        ObjectMapper mapper = new ObjectMapper();
        Parser uut = new Parser(mapper);
        realExample = uut.parse(s);
    }
    @Test
    void testFoo() throws Exception {
        GpxBuilder builder = new GpxBuilder();
        builder.add(realExample);
        builder.close();
        System.out.println(builder.getOutput());


    }

}