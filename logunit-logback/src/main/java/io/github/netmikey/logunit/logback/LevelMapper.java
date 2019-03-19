package io.github.netmikey.logunit.logback;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;

/**
 * Utility class to map between slf4j and logging implementation levels.
 */
public class LevelMapper {

    private static final Map<org.slf4j.event.Level, ch.qos.logback.classic.Level> LEVEL_MAPPING;

    private static final Map<ch.qos.logback.classic.Level, org.slf4j.event.Level> LEVEL_MAPPING_REVERSE;

    static {
        Map<org.slf4j.event.Level, ch.qos.logback.classic.Level> levelMapping = new HashMap<>();
        levelMapping.put(org.slf4j.event.Level.TRACE, ch.qos.logback.classic.Level.TRACE);
        levelMapping.put(org.slf4j.event.Level.DEBUG, ch.qos.logback.classic.Level.DEBUG);
        levelMapping.put(org.slf4j.event.Level.INFO, ch.qos.logback.classic.Level.INFO);
        levelMapping.put(org.slf4j.event.Level.WARN, ch.qos.logback.classic.Level.WARN);
        levelMapping.put(org.slf4j.event.Level.ERROR, ch.qos.logback.classic.Level.ERROR);

        LEVEL_MAPPING = Collections.unmodifiableMap(levelMapping);

        Map<ch.qos.logback.classic.Level, org.slf4j.event.Level> levelMappingReverse = new HashMap<>();
        levelMapping.forEach((key, value) -> levelMappingReverse.put(value, key));
        levelMappingReverse.put(ch.qos.logback.classic.Level.ALL, org.slf4j.event.Level.TRACE);
        levelMappingReverse.put(ch.qos.logback.classic.Level.OFF, org.slf4j.event.Level.ERROR);

        LEVEL_MAPPING_REVERSE = Collections.unmodifiableMap(levelMappingReverse);
    }

    /**
     * Map the specified Slf4j level to the appropriate implementation's level.
     * 
     * @param level
     *            The slf4j level.
     * @return The implementation's level.
     */
    public static Level mapLevel(org.slf4j.event.Level level) {
        Level result = LEVEL_MAPPING.get(level);
        if (result == null) {
            throw new IllegalArgumentException("Cannot map log level " + level + " to a Logback log level");
        }
        return result;
    }

    /**
     * Map the specified implementation level to the appropriate Slf4j level.
     * 
     * @param level
     *            The logging implementation's level.
     * @return The slf4j level.
     */
    public static org.slf4j.event.Level mapLevel(Level level) {
        org.slf4j.event.Level result = LEVEL_MAPPING_REVERSE.get(level);
        if (result == null) {
            throw new IllegalArgumentException("Cannot map Logback log level " + level + " to an slf4j log level");
        }
        return result;
    }
}
