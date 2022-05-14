package net.funkenburg.gc.backend.groundspeak.discover;

import net.funkenburg.gc.backend.groundspeak.discover.DiscoverUrlProvider;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GroundspeakDiscoverUrlProvider implements DiscoverUrlProvider {
    private final Random random = new Random();

    @Override
    public String get() {
        int index = random.nextInt(3) + 1;
        return "https://tiles0" + index + ".geocaching.com";
    }
}
