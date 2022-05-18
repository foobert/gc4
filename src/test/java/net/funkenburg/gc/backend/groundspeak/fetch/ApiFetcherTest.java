package net.funkenburg.gc.backend.groundspeak.fetch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.funkenburg.gc.backend.groundspeak.PremiumGeocache;
import org.junit.jupiter.api.Test;

class ApiFetcherTest {
    @Test
    void testFoo() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String gcCode = "GC12456";
        String s = objectMapper.writeValueAsString(new PremiumGeocache(gcCode));
        //        Geocache geocache = objectMapper.readValue(s, Geocache.class);
        System.out.println(s);
        //        System.out.println(geocache);
    }
}
