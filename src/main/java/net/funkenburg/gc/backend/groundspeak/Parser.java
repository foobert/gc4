package net.funkenburg.gc.backend.groundspeak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class Parser {
    private final ObjectMapper objectMapper;

    public Geocache parse(String raw) {
        try {
            return objectMapper.readValue(raw, Geocache.class);
        } catch (JsonProcessingException e) {
            log.error("Unable to parse geocache", e);
            return null;
        }
    }
}
