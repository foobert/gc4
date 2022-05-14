package net.funkenburg.gc.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {
    @Test
    void testFoo() throws IOException {
        Resource is = new DefaultResourceLoader().getResource("gc.json");
        String s = Files.readString(is.getFile().toPath());
        //        System.out.println(s);
        ObjectMapper mapper = new ObjectMapper();
        Parser uut = new Parser(mapper);
        Geocache parsed = uut.parse(s);

        assertThat(parsed.getCode()).isEqualTo("GC84KN9");
        assertThat(parsed.getDifficulty()).isEqualTo(3.5F);
        assertThat(parsed.getTerrain()).isEqualTo(2F);
        assertThat(parsed.isPremium()).isFalse();
        assertThat(parsed.isPublished()).isTrue();
        assertThat(parsed.isArchived()).isFalse();
        assertThat(parsed.getLatitude()).isEqualTo(51.329317D);
        assertThat(parsed.getLongitude()).isEqualTo(12.243567D);
        assertThat(parsed.getShortDescription()).isEqualTo("foo");
        assertThat(parsed.getLongDescription()).isEqualTo("bar");
        assertThat(parsed.getContainerType().getContainerTypeName()).isEqualTo("Micro");
    }
}
