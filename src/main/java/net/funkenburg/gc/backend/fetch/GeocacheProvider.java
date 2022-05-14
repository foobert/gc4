package net.funkenburg.gc.backend.fetch;

import java.util.Collection;
import java.util.Set;

public interface GeocacheProvider {
    Set<RawGeocache> getRawGeocaches(Collection<String> gcCodes);
}
