package net.funkenburg.gc.backend.discover;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("tiles")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Tile {
    @Id
    @Column("id")
    private Integer id;

    @Column("gccodes")
    private String[] gcCodes;

    @Column("ts")
    private Instant timestamp;
}
