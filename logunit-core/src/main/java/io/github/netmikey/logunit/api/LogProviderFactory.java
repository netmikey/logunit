package io.github.netmikey.logunit.api;

/**
 * Factory that creates {@link LogProvider} instances, used as SPI.
 */
public interface LogProviderFactory {
    /**
     * Create a new {@link LogProvider} instance.
     * 
     * @return The new instance.
     */
    public LogProvider create();
}
