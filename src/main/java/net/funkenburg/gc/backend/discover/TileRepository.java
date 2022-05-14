package net.funkenburg.gc.backend.discover;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface TileRepository extends CrudRepository<Tile, Integer> {
    @Modifying
    @Query(
            "INSERT INTO tiles (id, gccodes, ts) VALUES (:id, :gccodes, :ts) ON CONFLICT (id) DO UPDATE SET gccodes = :gccodes, ts = :ts")
    void update(@Param("id") int quadkey, @Param("gccodes") String[] gcCodes, Instant ts);
}
