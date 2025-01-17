package km.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private final Properties properties;

    public ConfigLoader(String configFilePath) throws IOException {
        properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(configFilePath)) {
            properties.load(inputStream);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getIntProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Property " + key + " is not found.");
        }
        return Integer.parseInt(value.trim());
    }

    public double getDoubleProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Property " + key + " is not found.");
        }
        return Double.parseDouble(value.trim());
    }

    public boolean getBooleanProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Property " + key + " is not found.");
        }
        return Boolean.parseBoolean(value.trim().toLowerCase());
    }
}