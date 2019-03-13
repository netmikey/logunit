package io.github.netmikey.logunit.logback;

import io.github.netmikey.logunit.api.LogProvider;
import io.github.netmikey.logunit.api.LogProviderFactory;

/**
 * Logback implementation of the {@link LogProviderFactory} SPI.
 */
public class LogbackLogProviderFactory implements LogProviderFactory {

    @Override
    public LogProvider create() {
        return new LogbackLogProvider();
    }

}
