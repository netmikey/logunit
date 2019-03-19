package io.github.netmikey.logunit.log4j2;

import io.github.netmikey.logunit.api.LogProvider;
import io.github.netmikey.logunit.api.LogProviderFactory;

/**
 * Log4j implementation of the {@link LogProviderFactory} SPI.
 */
public class Log4j2LogProviderFactory implements LogProviderFactory {

    @Override
    public LogProvider create() {
        return new Log4j2LogProvider();
    }

}
