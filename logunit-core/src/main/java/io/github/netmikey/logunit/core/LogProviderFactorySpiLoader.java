package io.github.netmikey.logunit.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import io.github.netmikey.logunit.api.LogProviderFactory;

/**
 * Lazily loads the {@link LogProviderFactory} SPI and provides a reference to
 * it.
 */
public class LogProviderFactorySpiLoader {

    private static ServiceLoader<LogProviderFactory> serviceLoader = ServiceLoader.load(LogProviderFactory.class);

    private static LogProviderFactory logProviderFactory;

    /**
     * Lazily loads the {@link LogProviderFactory} SPI. If it has already been
     * loaded, provides a reference to it.
     * 
     * @return The SPI instance.
     */
    public static LogProviderFactory getLogProviderFactory() {
        if (logProviderFactory == null) {
            synchronized (LogProviderFactorySpiLoader.class) {
                List<LogProviderFactory> logProviderFactories = new ArrayList<>();
                serviceLoader.forEach(logProviderFactories::add);

                if (logProviderFactories.isEmpty()) {
                    throw new IllegalStateException("Could not find any " + LogProviderFactory.class.getName()
                        + " implementation on the classpath. Do you have the LogUnit implementation module approriate "
                        + "for your logging framework in your classpath?");
                } else if (logProviderFactories.size() > 1) {
                    throw new IllegalStateException("Found more than one " + LogProviderFactory.class.getSimpleName()
                        + " implementation on the classpath. Found all of these: " + logProviderFactories);
                } else {
                    logProviderFactory = logProviderFactories.get(0);
                }
            }
        }

        return logProviderFactory;
    }
}
