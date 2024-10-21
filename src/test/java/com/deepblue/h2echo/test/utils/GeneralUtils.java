package com.deepblue.h2echo.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GeneralUtils {
    private static final ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private GeneralUtils() { }

    /***
     * Load a contents of a file from the resources area.
     *
     * @param path The path to the schema file.
     * @return The schema as a string.
     */
    public static String loadResourceTextFile(String path) throws FileNotFoundException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + path);

        if (resource.exists()) {
            return asString(resource);
        } else {
            throw new FileNotFoundException("Resource not found: " + path);
        }
    }

    /***
     * Get the contents of a resource as a string.
     *
     * @param resource The resource to read.
     * @return The contents of the resource as a string.
     */
    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /***
     * Deserialize an object from Json.
     *
     * @param jsonToConvert
     * @param clazz
     * @return The deserialized object.
     */
    public static Object fromJsonToObject(String jsonToConvert, Class<Object> clazz) {
        if (jsonToConvert == null) {
            throw new IllegalArgumentException("Parameter to serializeFromJson() cannot be null");
        }

        try {
            return jsonMapper.readValue(jsonToConvert, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("Error converting Json to %s", clazz.getSimpleName()), e);
        }
    }

    /***
     * Serialize an object to Json.
     *
     * @param jsonToConvert The object to serialize.
     * @return The Json representation of the object, or Null if the object is null.
     */
    public static String fromObjectToJson(Object jsonToConvert) {
        if (jsonToConvert == null) {
            throw new IllegalArgumentException("Parameter to serializeToJson() cannot be null");
        }

        try {
            return jsonMapper.writeValueAsString(jsonToConvert);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("Error converting %s to Json", jsonToConvert.getClass().getSimpleName()),
                    e);
        }
    }

    /***
     * Load properties from a string.
     *
     * @param propertiesString The string to load properties from.
     * @return The properties.
     */
    public static Properties loadPropertiesFromString(String propertiesString) throws IOException {
        try {
            Properties properties = new Properties();
            properties.load(new StringReader(propertiesString));
            return properties;
        } catch (IOException e) {
            throw new IOException("Error loading properties from string", e);
        }
    }

    /***
     * Load properties from a resource.
     *
     * @param filename The name of the file to load properties from.
     * @return The properties.
     */
    public static Properties loadPropertiesFromResource(String filename) throws IOException {
        String propText = loadResourceTextFile(filename);
        return loadPropertiesFromString(propText);
    }

    /***
     * Checks if a number is between a lower and upper bound.
     * @return True if the number is between the lower and upper bounds.
     */
    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    /***
     * Checks if an HTTP status code is a success code.
     * @return True if the status code is a success code.
     */
    public static boolean isSuccessHttpStatusCode(int statusCode) {
        return isBetween(statusCode, 200, 299);
    }

    /**
     * Compares two objects for shallow equality.
     *
     * @param obj1          The first NrmAuditResult object to compare.
     * @param obj2          The second NrmAuditResult object to compare.
     * @param printError    If true, prints error messages to the console when fields don't match.
     * @param excludeFields An array of field names to exclude from the comparison.
     * @return              true if the objects are equal, false otherwise.
     * @throws Exception    If there's an error accessing object fields during comparison.
     */
    public static boolean compareObjectsShallow(Object obj1, Object obj2, boolean printError, String... excludeFields) {
        // Compare NULLs.
        if (obj1 == null && obj2 == null) {
            return true;
        }

        if (obj1 == null || obj2 == null) {
            return false;
        }

        // Compare types.
        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }

        // Compare each field.
        boolean oneOrMoreFailedComparisons = false;
        List<String> excludeFieldsList = excludeFields != null
                ? Arrays.asList(excludeFields) : new ArrayList<String>();

        for (Field field : obj1.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);

                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);

                // Skip field if specified.
                if (excludeFields != null && excludeFieldsList.contains(field.getName())) {
                    continue;
                }

                // Compare for NULLs.
                if (value1 == null && value2 == null) {
                    continue;
                }

                if (value1 == null) {
                    if (printError) {
                        System.out.println("Field '" + field.getName() + "' does not match: NULL != '" + value2 + "'");
                    }

                    return false;
                }

                if (value2 == null) {
                    if (printError) {
                        System.out.println("Field '" + field.getName() + "' does not match: '" + value1 + "' != NULL");
                    }

                    return false;
                }

                // Check the values.
                if (!value1.equals(value2)) {
                    if (printError) {
                        String errorMsg = "Field '" + field.getName() + "' does not match: '" + value1 + "' != '" + value2 + "'";
                        System.out.println(errorMsg);
                    }

                    oneOrMoreFailedComparisons = true;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to fetch field '" + field.getName() + "' during comparison", e);
            }
        }

        return !oneOrMoreFailedComparisons;
    }
}
