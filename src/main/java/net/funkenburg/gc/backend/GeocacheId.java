package net.funkenburg.gc.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;


@Table("geocache_ids")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class GeocacheId {
    @Id
//    @GeneratedValue(generator = "quadkey")
//    @GenericGenerator(name = "quadkey", strategy = "com.example.demo.QuadkeyIdGenerator")
    @Column("id")
    private Integer id;

    @Transient
    private Tile tile;

    @Column("gccodes")
    private String[] geocacheIds;

    @Column("ts")
    private Instant timestamp;
}
