package io.github.netmikey.logunit.jul;

import io.github.netmikey.logunit.api.LogProvider;
import io.github.netmikey.logunit.api.LogProviderFactory;

/**
 * JUL implementation of the {@link LogProviderFactory} SPI.
 */
public class JulLogProviderFactory implements LogProviderFactory {

    @Override
    public LogProvider create() {
        return new JulLogProvider();
    }

}
