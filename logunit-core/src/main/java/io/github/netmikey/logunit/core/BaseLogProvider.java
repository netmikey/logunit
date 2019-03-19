package io.github.netmikey.logunit.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.event.Level;

import io.github.netmikey.logunit.api.LogProvider;

/**
 * Base class for log providers, handling the registration of type- and
 * name-based logger capturing.
 */
public abstract class BaseLogProvider implements LogProvider {

    private final Map<Class<?>, Level> loggerTypes = new HashMap<>();

    private final Map<String, Level> loggerNames = new HashMap<>();

    @Override
    public void provideForType(Class<?> type, Level level) {
        if (loggerTypes.containsKey(type)) {
            throw new IllegalArgumentException("LogProvider already providing LogEvents for Logger of type "
                + type.getName() + ". Each logger must only be captured once!");
        }
        loggerTypes.put(type, level);
    }

    @Override
    public void provideForLogger(String name, Level level) {
        if (loggerNames.containsKey(name)) {
            throw new IllegalArgumentException("LogProvider already providing LogEvents for Logger with name "
                + name + ". Each logger must only be captured once!");
        }
        loggerNames.put(name, level);
    }

    /**
     * Get the loggerTypes.
     * 
     * @return Returns the loggerTypes.
     */
    protected Map<Class<?>, Level> getLoggerTypes() {
        return loggerTypes;
    }

    /**
     * Get the loggerNames.
     * 
     * @return Returns the loggerNames.
     */
    protected Map<String, Level> getLoggerNames() {
        return loggerNames;
    }

}
