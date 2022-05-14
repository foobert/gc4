package net.funkenburg.gc.backend;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RawGeocacheRepository extends CrudRepository<RawGeocache, String> {
    @Modifying
    @Query(
            "INSERT INTO raw_geocaches (id, raw, ts) VALUES (:id, :raw::JSON, :ts) ON CONFLICT (id) DO UPDATE SET raw = :raw::JSON, ts = :ts")
    void update(String id, String raw, Instant ts);
}
