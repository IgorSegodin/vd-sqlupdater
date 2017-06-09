package de.vdata.sqlupdater.util;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Wrapper with common property logic: trims string values, returns default values, validates mandatory parameters.
 *
 * @author isegodin
 */
public class PropertiesWrapper {

    private static final Pattern VALID_STRING_PATTERN = Pattern.compile("[^\\S\\s]+");

    private final Properties properties;

    public PropertiesWrapper(Properties properties) {
        this.properties = properties;
    }

    public String get(String key) {
        return getInternal(key, false, null);
    }

    public String get(String key, String defaultValue) {
        return getInternal(key, false, defaultValue);
    }

    public String getStrict(String key) {
        return getInternal(key, true, null);
    }

    private String getInternal(String key, boolean strict, String defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        if (value == null || !VALID_STRING_PATTERN.matcher(value).matches()) {
            if (strict) {
                throw new RuntimeException("Please, specify property: " + key);
            }
            if (defaultValue != null) {
                System.out.println("Using default property value - " + key + "=" + defaultValue);
                value = defaultValue;
            } else {
                value = null;
            }
        }
        return value;
    }
}
