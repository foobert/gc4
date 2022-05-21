package net.funkenburg.gc.backend.fetch;

import java.util.Collection;
import java.util.stream.Stream;

public interface GeocacheProvider {
    Stream<RawGeocache> getRawGeocaches(Collection<String> gcCodes);
}
