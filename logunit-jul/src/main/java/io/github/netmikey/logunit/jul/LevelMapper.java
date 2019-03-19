package io.github.netmikey.logunit.jul;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Utility class to map between slf4j and logging implementation levels.
 */
public class LevelMapper {

    private static final Map<org.slf4j.event.Level, java.util.logging.Level> LEVEL_MAPPING;

    private static final Map<java.util.logging.Level, org.slf4j.event.Level> LEVEL_MAPPING_REVERSE;

    static {
        Map<org.slf4j.event.Level, java.util.logging.Level> levelMapping = new HashMap<>();
        levelMapping.put(org.slf4j.event.Level.TRACE, java.util.logging.Level.FINEST);
        levelMapping.put(org.slf4j.event.Level.DEBUG, java.util.logging.Level.FINE);
        levelMapping.put(org.slf4j.event.Level.INFO, java.util.logging.Level.INFO);
        levelMapping.put(org.slf4j.event.Level.WARN, java.util.logging.Level.WARNING);
        levelMapping.put(org.slf4j.event.Level.ERROR, java.util.logging.Level.SEVERE);

        LEVEL_MAPPING = Collections.unmodifiableMap(levelMapping);

        Map<java.util.logging.Level, org.slf4j.event.Level> levelMappingReverse = new HashMap<>();
        levelMapping.forEach((key, value) -> levelMappingReverse.put(value, key));
        levelMappingReverse.put(java.util.logging.Level.CONFIG, org.slf4j.event.Level.INFO);
        levelMappingReverse.put(java.util.logging.Level.FINER, org.slf4j.event.Level.DEBUG);

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
            throw new IllegalArgumentException("Cannot map log level " + level + " to a JUL log level");
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
            throw new IllegalArgumentException("Cannot map JUL log level " + level + " to an slf4j log level");
        }
        return result;
    }

}
