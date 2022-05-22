package net.funkenburg.gc.backend.gpx;

import net.funkenburg.gc.backend.groundspeak.Geocache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringCleanerTest {
    private Geocache gc;
    private StringCleaner uut;

    @BeforeEach
    void setup() {
        gc = new Geocache();
        uut = new StringCleaner(gc);
    }

    @Test
    void testCodeSkipsPrefix() {
        gc.setCode("GC12345");

        assertThat(uut.code()).isEqualTo("12345");
    }

    @Test
    void testHintCleansHtmlTags() {
        gc.setEncodedHints("foo<br/>bar<br>baz");
        assertThat(uut.hint()).isEqualTo("foo bar baz");
    }

    @Test
    void testHintRemovesAngleBrackets() {
        gc.setEncodedHints("foo<bar");
        assertThat(uut.hint()).isEqualTo("foobar");
    }
}
