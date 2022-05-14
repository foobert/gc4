package net.funkenburg.gc.backend.groundspeak.auth;

@FunctionalInterface
public interface GroundspeakAccessTokenProvider {
    String get();
}
