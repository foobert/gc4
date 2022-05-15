package net.funkenburg.gc.backend.groundspeak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class PremiumGeocache {
    private String code;

    @JsonProperty("IsPremium")
    private boolean isPremium = true;

    public PremiumGeocache(String code) {
        this.code = code;
    }

    public PremiumGeocache() {}
}
