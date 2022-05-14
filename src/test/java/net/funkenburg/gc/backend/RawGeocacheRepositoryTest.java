package net.funkenburg.gc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
class RawGeocacheRepositoryTest {
    @Autowired
    private RawGeocacheRepository repo;

    @Test
    void foo() {
        repo.update("GC123", "{\"foo\":42}", Instant.now());
    }

}