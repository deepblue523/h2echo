package com.deepblue523.h2echo;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import com.deepblue523.h2echo.annotations.EchoDao;
import com.deepblue523.h2echo.annotations.EnableH2Echo;
import com.deepblue523.h2echo.dialects.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/***
 * This class contains support methods and data for unit tests.  It offers
 * integration-test-like support within unit tests by running the SQL
 * scripts on an embedded H2 database.
 * <p>
 * This class may be temporary and may be removed in the future.
 * <p>
 * Important note:  Every unit test setup that calls this class's
 * runFlywayScripts() will be executed with a totally clean
 * schema based upon the SQL scripts.  In that case it is assumed that
 * the test target is an H2 database since there are some script adjustments
 * made while running the statements.  This whole thing is in order to avoid
 * maintaining a separate set of scripts for H2, but this approach works.
 */
public class H2Echo {
  private static String DEFAULT_SCRIPT_PATH = "db/migrations/";
  /***
   * Get a list of files in the resources directory.
   *
   * @return Array of files, or empty array if none found.
   */
  public static File[] getFileList(String pathName) {
    try {
      ClassLoader classLoader = H2Echo.class.getClassLoader();
      URL resourceUrl = classLoader.getResource(pathName);

      if (resourceUrl != null) {
        File resourcesDir = new File(resourceUrl.toURI());
        File[] files = resourcesDir.listFiles();

        if (files != null) {
          return files;
        }
      }
    }
    catch (URISyntaxException e) { // For now, treat this as "none found".
    }

    return new File[0];
  }

  /***
   * Sort SQL scripts by version number. Note that a simple sort by
   * filename won't work.  We need to sort numerically by the version number.
   * So there is a tiny bit of parsing here.
   *
   * @param fileList The list of files to sort.
   * @return The same list, sorted.
   */
  public static File[] sortSqlScriptsByVersion(File[] fileList) {
    Arrays.sort(
        fileList,
        (f1, f2) -> {
          // Remove the "V" prefix.
          String f1NoV = f1.getName().substring(1);
          String f2NoV = f2.getName().substring(1);

          // Find the version number terminator.
          int f1Dot = f1NoV.indexOf("__");
          int f2Dot = f2NoV.indexOf("__");

          String f1VersionStr = f1NoV.substring(0, f1Dot);
          String f2VersionStr = f2NoV.substring(0, f2Dot);

          Double f1Version = Double.parseDouble(f1VersionStr);
          Double f2Version = Double.parseDouble(f2VersionStr);

          return f1Version.compareTo(f2Version);
        });

    return fileList;
  }

  /***
   * Construct the test database for our tests.  If it already exists, it will be
   * dropped and recreated freshly.
   * <p>
   * The purpose of this method is to set up a fresh, config-free H2 test database
   * using existing SQL scripts:
   * <p>
   *    - Clearing any existing schema
   *    - Running all SQL SQL scripts in order
   *    - Ensuring a clean, consistent database state for testing
   * <p>
   * Process:
   * <p>
   * 1. The method  `runFlywayScripts`  takes two parameters:
   *    -  `jdbcTemplate` : A JdbcTemplate object for database operations
   *    -  `displayLog` : A boolean flag to control logging
   * <p>
   * 2. It checks if the database has been created before using a static boolean
   *    `hasDbBeenCreatedBefore` . If true, it sets `displayLog` to false to avoid
   *    cluttering the console.
   * <p>
   * 3. If `displayLog` is true, it prints a message indicating the creation of a
   *    fresh test database schema.
   * <p>
   * 4. It calls `getFileList()` to retrieve a list of SQL SQL scripts from
   *    the `FLYWAY_SCRIPT_PATH` .
   * <p>
   * 5. The script files are then sorted using `sortSqlScriptsByVersion()`, which
   *    orders them based on their version numbers (e.g., V1.0__, V1.1__, etc.).
   * <p>
   * 6. The method sets the schema to PUBLIC.
   * <p>
   * 7. It calls  `runFlywayScriptsOnH2()`  to execute the sorted SQL scripts on an
   *    H2 database.
   * <p>
   * 8. Finally, it sets `hasDbBeenCreatedBefore` to true, indicating that the database
   *    has been created.
   * <p>
   * This approach allows for reproducible test environments by always starting with a known database state.
   */
  public static void runSqlScriptsOnH2(
          JdbcTemplate jdbcTemplate, String scriptPath, ScriptSyntax syntax, boolean displayLog) {
    // Create appropriate syntax plugin.
    DialectRunnerBase dialectRunner = null;

    if (syntax == ScriptSyntax.H2)
      dialectRunner = new H2Runner();
    else if (syntax == ScriptSyntax.MYSQL)
      dialectRunner = new MySqlRunner();
    else if (syntax == ScriptSyntax.MARIA_DB)
      dialectRunner = new MariaDbRunner();
    else
      dialectRunner = new MariaDbRunner();

    // Get a list of SQL scripts and sort them by "Vnn.mm__". prefix.
    File[] fileList = getFileList(scriptPath);
    File[] fileListSorted = sortSqlScriptsByVersion(fileList);

    // FINALLY - run the scripts!!!   Yabba Dabba Dooo!!!
    dialectRunner.runSqlScriptsOnH2(jdbcTemplate, fileListSorted, displayLog);
  }

  public static void runSqlScriptsOnH2(
          JdbcTemplate jdbcTemplate, ScriptSyntax syntax, boolean displayLog) {
    runSqlScriptsOnH2(jdbcTemplate, DEFAULT_SCRIPT_PATH, syntax, displayLog);
  }

  public static void runSqlScriptsOnH2(JdbcTemplate jdbcTemplate, boolean displayLog) {
    runSqlScriptsOnH2(jdbcTemplate, DEFAULT_SCRIPT_PATH, ScriptSyntax.MARIA_DB, displayLog);
  }

  public static void runSqlScriptsOnH2(JdbcTemplate jdbcTemplate, String scriptPath, boolean displayLog) {
    runSqlScriptsOnH2(jdbcTemplate, scriptPath, ScriptSyntax.MARIA_DB, displayLog);
  }

  /**
   * Runs SQL scripts on an object annotated with @ChimpWired.
   * This method sets up an in-memory H2 database, injects DAOs into fields
   * annotated with @EchoDao, and executes SQL SQL scripts.
   *
   * @param object The object to process. Must be annotated with @ChimpWired.
   * @param displayLog A boolean flag to control logging during script execution.
   * @throws IllegalArgumentException If the object is not properly annotated,
   *         if there are issues with DAO instantiation, or if field injection fails.
   */
  public static void echoDaosOnObject(Object object, boolean displayLog) {
    if (object == null) {
      return;
    }

    // Get the expected script syntax from the annotation.
    String defaultScriptPath = DEFAULT_SCRIPT_PATH;
    ScriptSyntax defaultSyntax = ScriptSyntax.MARIA_DB;

    if (object.getClass().isAnnotationPresent(EnableH2Echo.class)) {
      defaultScriptPath = object.getClass().getAnnotation(EnableH2Echo.class).scriptPath();
      defaultSyntax = object.getClass().getAnnotation(EnableH2Echo.class).syntax();
    }

    if (defaultScriptPath == null) {
        throw new IllegalArgumentException("For @EnableH2Echo, a Null scriptPath is not supported.");
    }

    // Get the datasource we'll use to write to H2.  The annotated DAOs will also
    // be tied to this data source.  For now, we'll use the default H2 credentials.
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:tmp;DB_CLOSE_DELAY=-1");
    dataSource.setUsername("sa");
    dataSource.setPassword("");

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

      // Run the SQL scripts.
    // Look for fields annotated with @EchoDao.
    for (Field field : object.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(EchoDao.class)) {
        // Get the expected script syntax from the annotation.
        String scriptPath = defaultScriptPath;
        ScriptSyntax scriptSyntax = field.getAnnotation(EchoDao.class).syntax();

        if (scriptSyntax == ScriptSyntax.DEFAULT) {
          defaultScriptPath = object.getClass().getAnnotation(EnableH2Echo.class).scriptPath();
          scriptSyntax = defaultSyntax;
        }

        // Construct our DAO object using the datasource we prepared.
        field.setAccessible(true);
        Constructor<Object> constructor = null;

        try {
            constructor = (Constructor<Object>) field.getType().getConstructor(JdbcTemplate.class);
        }
        catch (NoSuchMethodException e) {
          throw new IllegalArgumentException(
                  "The class " + object.getClass().getName() + " should have constructor accepting JdbcTemplate");
        }

        Object dao = null;
        try {
          dao = constructor.newInstance(jdbcTemplate);

          // Run the SQL scripts.
          runSqlScriptsOnH2(jdbcTemplate, scriptSyntax, displayLog);
        }
        catch (InstantiationException e) {
          throw new IllegalArgumentException(
                  "IllegalArgumentException while attempt to create instance of " + object.getClass().getName());
        }
        catch (IllegalAccessException e) {
          throw new IllegalArgumentException(
                  "IllegalAccessException while attempt to create instance of " + object.getClass().getName());
        }
        catch (InvocationTargetException e) {
          throw new IllegalArgumentException(
                  "InvocationTargetException while attempt to create instance of " + object.getClass().getName());
        }

        // Finally, set the field in the object to the generated DAO.
        try {
            field.set(object, dao);
        }
        catch (IllegalAccessException e) {
          throw new IllegalArgumentException(
                  "IllegalAccessException while attempt to set field '" + field.getName() + "' in class " + object.getClass().getName());
        }
      }
    }
  }
}
