package net.funkenburg.gc.backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class Geocache {
    private String code;

    private String name;

    private float terrain;
    private float difficulty;

    private boolean archived;

    private boolean isLocked;
    private boolean isPremium;
    private boolean isPublished;

    private double latitude;
    private double longitude;

    private String shortDescription;
    private String longDescription;

    private String hint;

    private ContainerType containerType;
    private CacheType cacheType;

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    public static class ContainerType {
        private int containerTypeId;
        private String containerTypeName;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    public static class CacheType {
        private int geocacheTypeId;
        private String geocacheTypeName;
    }

    public GeocacheType getGeocacheType() {
        return GeocacheType.of(cacheType.getGeocacheTypeId());
    }
}
