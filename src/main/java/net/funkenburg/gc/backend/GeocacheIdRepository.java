package net.funkenburg.gc.backend;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Set;

@Repository
public interface GeocacheIdRepository extends CrudRepository<GeocacheId, Integer> {
    @Modifying
    @Query("INSERT INTO geocache_ids (id, gccodes, ts) VALUES (:id, :gccodes, :ts) ON CONFLICT (id) DO UPDATE SET gccodes = :gccodes, ts = :ts")
    void update(@Param("id") int quadkey, @Param("gccodes") String[] gcCodes, Instant ts);
}
