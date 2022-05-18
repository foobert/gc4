package net.funkenburg.gc.backend.gpx;

import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.groundspeak.GeocacheType;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Slf4j
public class GpiBuilder {
    private final ResourceLoader resourceLoader;

    private final String gpsBabelArgs;

    public GpiBuilder(
            ResourceLoader resourceLoader, @Value("${gpsbabel.args}") String gpsBabelArgs) {
        this.resourceLoader = resourceLoader;
        this.gpsBabelArgs = gpsBabelArgs;
    }

    public byte[] convert(String gpx, GeocacheType type) throws IOException {
        Path workspace = Files.createTempDirectory("gpi");
        try {
            copyBitmap(type, workspace.resolve("image.bmp").toAbsolutePath());
            Path inputPath = workspace.resolve("input.gpx").toAbsolutePath();
            Path outputPath = workspace.resolve("output.gpi").toAbsolutePath();
            Files.writeString(inputPath, gpx);
            var gpsbabel =
                    new ProcessBuilder(gpsBabelArgs.split(" "))
                            .directory(workspace.toFile())
                            .redirectErrorStream(true)
                            .start();
            gpsbabel.waitFor();
            logOutput(gpsbabel);
            log.info("gpsbabel exit value {}", gpsbabel.exitValue());
            return Files.readAllBytes(outputPath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } finally {
            deleteDirectory(workspace);
        }
    }

    private void logOutput(Process gpsbabel) throws IOException {
        try (var bufferedReader =
                new BufferedReader(new InputStreamReader(gpsbabel.getInputStream()))) {
            while (bufferedReader.ready()) {
                log.info("gpsbabel: {}", bufferedReader.readLine());
            }
        }
    }

    private void copyBitmap(GeocacheType type, Path output) throws IOException {
        try (var is =
                        resourceLoader
                                .getResource(
                                        "classpath:gpi/"
                                                + type.name().toLowerCase(Locale.ROOT)
                                                + ".bmp")
                                .getInputStream();
                var os = new FileOutputStream(output.toFile())) {
            Streams.copy(is, os, true);
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
