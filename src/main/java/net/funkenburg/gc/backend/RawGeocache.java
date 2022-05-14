package net.funkenburg.gc.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;

@Table("raw_geocaches")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RawGeocache {
    @Id
    @Column("id")
    private String id;

    @Column("raw")
    private Object raw;

    @Transient
    public String getRawString() {
        return Objects.toString(raw);
    }

    @Column("ts")
    private Instant timestamp;
}
