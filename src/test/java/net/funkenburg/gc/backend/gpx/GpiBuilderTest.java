package net.funkenburg.gc.backend.gpx;

import net.funkenburg.gc.backend.groundspeak.GeocacheType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class GpiBuilderTest {
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    void testRunsCommandWithCorrectInput() throws Exception {
        String gpx = "gpx";

        GpiBuilder gpiBuilder = new GpiBuilder(resourceLoader, "cp input.gpx output.gpi");

        byte[] output = gpiBuilder.convert(gpx, GeocacheType.TRADITIONAL);
        assertThat(new String(output)).isEqualTo(gpx);
    }

    @Test
    void testUsesCorrectImage() throws Exception {
        GpiBuilder gpiBuilder = new GpiBuilder(resourceLoader, "cp image.bmp output.gpi");

        byte[] output = gpiBuilder.convert("", GeocacheType.TRADITIONAL);
        assertThat(output).isNotEmpty();
    }
}
