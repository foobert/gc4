package net.funkenburg.gc.backend.gpx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.groundspeak.GeocacheType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GpiBuilder {
    private final ResourceLoader resourceLoader;

    public byte[] convert(String gpx, GeocacheType type) throws IOException {
        Path workspace = Files.createTempDirectory("gpi");
        try {
            Path bitmapPath =
                    resourceLoader
                            .getResource(
                                    "classpath:gpi/"
                                            + type.name().toLowerCase(Locale.ROOT)
                                            + ".bmp")
                            .getFile()
                            .toPath()
                            .toAbsolutePath();
            Path inputPath = workspace.resolve("input.gpx").toAbsolutePath();
            Path outputPath = workspace.resolve("output.gpx").toAbsolutePath();
            Files.copy(bitmapPath, workspace.resolve("image.bmp"));
            Files.writeString(inputPath, gpx);
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "docker",
                            "run",
                            "--rm",
                            "-v",
                            workspace.toString() + ":/app",
                            "-w",
                            "/app",
                            "jamesmstone/gpsbabel",
                            "-i",
                            "gpx",
                            "-f",
                            "input.gpx",
                            "-o",
                            "garmin_gpi,bitmap=image.bmp,sleep=1",
                            "-F",
                            "output.gpx");
            Process start = processBuilder.start();
            start.waitFor();
            return Files.readAllBytes(outputPath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } finally {
            deleteDirectory(workspace);
        }
    }

    private void deleteDirectory(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(
                            path1 -> {
                                try {
                                    Files.delete(path1);
                                } catch (IOException e) {
                                    log.error("Unable to delete directory {}", path, e);
                                }
                            });
        } catch (IOException e) {
            log.error("Unable to delete directory {}", path, e);
        }
    }
}
