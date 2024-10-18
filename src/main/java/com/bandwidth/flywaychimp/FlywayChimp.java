package com.jkessler523.flywaychimp;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.FileCopyUtils;

/***
 * This class contains support methods and data for unit tests.  It offers
 * integration-test-like support within unit tests by running the Flyway
 * scripts on an embedded H2 database.
 * <p>
 * This class may be temporary and may be removed in the future.
 * <p>
 * Important note:  Every unit test setup that calls this class's
 * runFlywayScripts() will be executed with a totally clean
 * schema based upon the Flyway scripts.  In that case it is assumed that
 * the test target is an H2 database since there are some script adjustments
 * made while running the statements.  This whole thing is in order to avoid
 * maintaining a separate set of scripts for H2, but this approach works.
 */
public class FlywayChimp {
  // Used to track if the database has been created before.  If it has, we don't need to
  // log script execution details.
  private static boolean hasDbBeenCreatedBefore = false;

  private static final String FLYWAY_SCRIPT_PATH = "flyway/migrations";

  /***
   * Get a list of files in the resources directory.
   *
   * @return Array of files, or empty array if none found.
   */
  public static File[] getFileList(String pathName) {
    try {
      ClassLoader classLoader = FlywayChimp.class.getClassLoader();
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
   * Sort Flyway scripts by version number. Note that a simple sort by
   * filename won't work.  We need to sort numerically by the version number.
   * So there is a tiny bit of parsing here.
   *
   * @param fileList The list of files to sort.
   * @return The same list, sorted.
   */
  public static File[] sortFlywayScriptsByVersion(File[] fileList) {
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
   * Run Flyway scripts on an H2 database.  This is a bit of a hack, but it's
   * necessary because H2 doesn't support all the SQL syntax that MariaDB
   * does.  So we need to make some adjustments to the SQL before running it.
   * <p>
   * The nice thing about this method is that it is very  to use.  There is no
   * setup - secrets, variables, nothing.  It just runs.
   * <p>
   * Given a list of Flyway scripts, this method does the following types of things:
   * <p>
   *    * Iterates through each Flyway script file, reads its contents, and processes
   *      the SQL statements.  For each one, it performs various adjustments to the SQL
   *      statements to make them compatible with H2 database:
   * <p>
   *         - Adjusts primary key syntax
   *         - Filters out unsupported statements
   *         - Rewrites CREATE TABLE and ALTER TABLE statements
   *         - Removes "UNSIGNED" from column definitions
   *         - Handles schema-related statements
   *         - For right now, skips trigger, sproc and user creation statements.
   * <p>
   * 9. The method keeps track of various statistics:
   * <p>
   *    - Total statements run
   *    - Total statements skipped
   *    - Total errors encountered
   * <p>
   * It also writes a little bit of info to the console.  We don't normally do this,
   * but it can be useful, as in this case it helps use see what is happening with the SQL.
   * There isn't much output from it but there is enough to be helpful.  It is only printed
   * if tests are running and if the corresponding method parameter is set.
   * <p>
   * SQL errors do not stop processing - they are collected and summarized in the console.
   * Here is a sample of the output:
   * <p>
   *    Running DB script on H2: V1.0__add_voice_config_initial_schema_and_user.sql
   *    Running DB script on H2: V2.0__add_profile_id_generation_table_and_core2_account_profile_mapping_table.sql
   *    Running DB script on H2: V3.0__add_origination_route_plan_schema.sql
   *    Running DB script on H2: V4.0__add_stir_shaken_table.sql
   *    Running DB script on H2: V5.0__alter_voice_config_telephone_number_table.sql
   *    Running DB script on H2: V6.0__add_cnam_and_cnam_history_tables.sql
   * <p>
   *    Total SQL statements skipped count: 4
   *    Total SQL error count: 4
   *    SQL errors:
   * <p>
   *    ALTER TABLE VOICE.PHONE_NUMBER_INVENTORY -  expected "ADD, SET, RENAME, DROP, ALTER"
   *    CREATE TRIGGER VOICE.CNAM_AFTER_INSERT -  expected "QUEUE, NOWAIT, AS, CALL"
   *    CREATE TRIGGER VOICE.CNAM_AFTER_UPDATE -  expected "QUEUE, NOWAIT, AS, CALL"
   *    CREATE TRIGGER VOICE.CNAM_AFTER_DELETE -  expected "QUEUE, NOWAIT, AS, CALL"
   * <p>
   * @param jdbcTemplate The JDBC template to use.
   * @param fileList The list of script files to run.
   */
  public static void runFlywayScriptsOnH2(
      JdbcTemplate jdbcTemplate, File[] fileList, boolean displayLog) {
    // Load each script.
    int totalStmtsSkipped = 0;
    int totalStmtsRun = 0;

    List<String> errorLogList = new ArrayList<String>();
    int totalErrors = 0;

    // ---[ Look at each Flyway script file ]---
    // At this point this should be sorted by version.
    for (File file : fileList) {
      if (displayLog) {
        System.out.println("Running DB script on H2: " + file.getName());
      }

      // ---[ Read the file contents ]---
      String sqlScriptContents;
      try (Reader reader = new FileReader(file)) {
        sqlScriptContents = FileCopyUtils.copyToString(reader);
      } catch (IOException e) { // Ignore for now.
        throw new UncheckedIOException(e);
      }

      // ---[ Split into individual SQL statements from Flyway script]---
      // Break on the ';' character ... and remove any leading spaces ...
      // ... and condense spaces ... and convert to uppercase for easier RegEx/parsing.
      boolean insideSprocDef = false;
      sqlScriptContents = sqlScriptContents.replaceAll("  ", " ");

      String[] sqlStatementList = sqlScriptContents.split(";");
      for (String sqlStatement : sqlStatementList) {
        // ---[ Weed out statements that we are not going to process for H2 for now ]---
        // Make any adjustments to the SQL so they work in H2.
        // Remove comments in order to simplify the regexes.
        String originalStmtToRunAdj = removeComments(sqlStatement);
        String stmtNoLeadingSpaces = originalStmtToRunAdj.stripLeading().toUpperCase();

        // Handle stored procedure definitions.  For now, we just skip them.
        if (stmtNoLeadingSpaces.contains("CREATE PROCEDURE")) {
          insideSprocDef = true;
          continue;
        } else if (stmtNoLeadingSpaces.startsWith("END")) {
          insideSprocDef = false;
          totalStmtsSkipped++;
          continue;
        } else if (insideSprocDef) {
          continue;
        }

        // Kick out if we don't support the statement.
        if (!doWeSupportStatementInH2(stmtNoLeadingSpaces)) {
          totalStmtsSkipped++;
          continue;
        }

        // Each statement may need to be broken out into smaller, more
        // primitive ones.  Especially ALTER statements.
        List<String> finalSqlListToRun = new ArrayList<String>();

        // ---[ CREATE TABLE adjustments ]---
        if (originalStmtToRunAdj.contains("CREATE TABLE")) {
          // Drop any existing table prior to recreating it.
          addDropTableIfExistsStatement(originalStmtToRunAdj, finalSqlListToRun);

          // Primary key syntax on CREATE TABLE is different between H2/MariaDB.
          originalStmtToRunAdj = adjustPrimaryKeySyntax(originalStmtToRunAdj);

          // Other stuff.
          originalStmtToRunAdj = removeIndexes(originalStmtToRunAdj);

          finalSqlListToRun.add(originalStmtToRunAdj.stripLeading());
        }

        // ---[ ALTER TABLE adjustments ]---
        else if (originalStmtToRunAdj.contains("ALTER TABLE")) {
          breakAlterIntoSmallerPieces(originalStmtToRunAdj, finalSqlListToRun);
        }

        // ---[ Other supported statements ]---
        else {
          finalSqlListToRun.add(originalStmtToRunAdj.stripLeading());
        }

        // ---[ It's possible there is nothing to run at this point ]---
        if (finalSqlListToRun.isEmpty()) {
          totalStmtsSkipped++;
          continue;
        }

        // Run the statements.  For now, ignore errors.  This is currently known
        // to be a problem with the H2 database and some ALTER statements.
        for (String sqlToRun : finalSqlListToRun) {
          try {
            // Final formatting.
            // ... replace any remaining back-to-back commas, dangling commas, convert
            // \r\n to spaces, etc.  Things can happen based upon previous replacements in some
            // cases.
            String sqlToRunAdj = sqlToRun;
            sqlToRunAdj = sqlToRunAdj.replaceAll("\\,\\s*?\\,", ",");
            sqlToRunAdj = sqlToRunAdj.replaceAll("\\,\\s*?\\)", ")");
            sqlToRunAdj = sqlToRunAdj.replaceAll("[\\r\\n]", " ");
            sqlToRunAdj = sqlToRunAdj.replaceAll("\\s{2,}", " ");
            sqlToRunAdj = sqlToRunAdj.replaceAll("\\sIGNORE\\s*", " ");

            totalStmtsRun++;
            jdbcTemplate.execute(sqlToRunAdj);
          } catch (Exception e) {
            // An "IGNORE" option in the SQL will be considered okay.  Normally
            // H2 would throw an exception.
            if (sqlToRun.contains("IGNORE")) {
              continue;
            }

            // ---[ Collect bad lines for a console log ]---
            totalErrors++;
            errorLogList.add(file.getName());
            errorLogList.add("  - " + sqlToRun);

            String completeExMsg = e.getCause() != null ? e.getCause().getMessage() : "";
            if (!completeExMsg.isBlank()) {
              errorLogList.add("  - " + completeExMsg.substring(0, completeExMsg.indexOf(";")));
            }

            errorLogList.add("");
          }
        }
      }
    }

    // Summary.
    if (displayLog) {
      System.out.println("\n---[ Summary of SQL script execution ]---");
      System.out.println("(adjusted for refactored ALTERs for H2 compatibility)");
      System.out.println("Total SQL statements run count: " + totalStmtsRun);
      System.out.println("Total SQL statements skipped count: " + totalStmtsSkipped);
      System.out.println("Total SQL error count: " + totalErrors);

      if (!errorLogList.isEmpty()) {
        System.out.println("\nSQL errors:");

        for (String error : errorLogList) {
          System.out.println(error);
        }
      }
    }
  }

  /***
   * Adjust primary key syntax so H2 likes them.
   * <p>
   * 1. It takes a SQL statement as input (String stmt).
   * <p>
   * 2. It uses regular expressions to make several modifications to the statement:
   * <p>
   *    a. Removes any "CONSTRAINT xxx PRIMARY KEY (xxx)" clauses:
   *       - This is done using the first replaceAll() method.
   *       - It matches and removes patterns like "CONSTRAINT table_pk PRIMARY KEY (id)".
   * <p>
   *    b. Removes any remaining "PRIMARY KEY (xxx)" clauses:
   *       - This is done using the second replaceAll() method.
   *       - It matches and removes patterns like "PRIMARY KEY (id)".
   * <p>
   *    c. Changes AUTO_INCREMENT fields to IDENTITY fields with PRIMARY KEY:
   *       - This is done using the third replaceAll() method.
   *       - It replaces patterns like "INT UNSIGNED NOT NULL AUTO_INCREMENT" with
   *         "IDENTITY NOT NULL PRIMARY KEY".
   *       - It also handles BIGINT variants.
   * <p>
   *    d. Removes the "UNSIGNED" keyword:
   *       - This is done using the fourth replaceAll() method.
   *       - It simply removes the word "UNSIGNED" from the statement.
   * <p>
   * 3. Finally, it returns the modified SQL statement.
   * <p>
   * The purpose of this method is to adapt SQL statements that might be written for
   * other databases (like MySQL) to work with H2, which has different syntax for
   * primary keys and auto-incrementing fields. This kind of adjustment is common
   * when building database-agnostic applications or when migrating between different
   * database systems.
   *
   * @param stmt The SQL statement to adjust.
   * @return The adjusted SQL statement.
   */
  private static String adjustPrimaryKeySyntax(String stmt) {
    // Remove CONSTRAINT xxx PRIMARY KEY (xxx) from the statement.
    stmt = stmt.replaceAll("CONSTRAINT\\s*[A-Za-z0-9_]*\\s*PRIMARY\\s*KEY\\s*?\\(.*?\\),{0,1}", "");

    // Remove any remaining PRIMARY KEY literal from the statement.
    stmt = stmt.replaceAll("PRIMARY KEY\\s*?\\(.*?\\)", "");

    // Change AUTO_INCREMENT field to a primary key IDENTITY field.
    stmt =
        stmt.replaceAll(
            " (BIG){0,1}INT\\s*?UNSIGNED\\s*?NOT\\s*?NULL\\s*?AUTO_INCREMENT",
            "IDENTITY NOT NULL PRIMARY KEY");

    stmt = stmt.replaceAll("UNSIGNED", "");

    return stmt;
  }

  /***
   * Remove index declarations from CREATE TABLE statements.  We won't apply
   * the indexes in H2 for now.
   *
   * @param stmt The SQL statement to adjust.
   * @return The adjusted SQL statement.
   */
  private static String removeIndexes(String stmt) {
    // Remove index declarations on CREATE TABLE statements.
    stmt = stmt.replaceAll("INDEX\\s*[A-Za-z0-9_]*\\s\\(.*?\\)", "");

    return stmt;
  }

  /***
   * Determine if we support a given statement in H2 and need in our tests.
   * Supporting fewer features give fewer things to fail.
   *
   * @param stmt The SQL statement to check.
   * @return True if we support it, false if not.
   */
  private static boolean doWeSupportStatementInH2(String stmt) {
    return (stmt.startsWith("CREATE SCHEMA")
        || stmt.startsWith("CREATE TABLE")
        || stmt.startsWith("DROP TABLE")
        || stmt.startsWith("ALTER TABLE")
        || stmt.startsWith("INSERT")
        || stmt.startsWith("DELETE"));
  }

  /***
   * Determine if we support a given ALTER statement in H2 and need in our tests.
   * Supporting fewer features gives fewer things to fail.
   *
   * @param stmt The SQL statement to check.
   * @return True if we support it, false if not.
   */
  private static boolean doWeSupportAlterStatementInH2(String stmt) {
    return (stmt.contains("ADD COLUMN")
        || stmt.contains("CHANGE COLUMN")
        || stmt.contains("RENAME TO")
        || stmt.contains("DROP PRIMARY KEY")
        || stmt.contains("ADD PRIMARY KEY")
        || stmt.contains("DROP FOREIGN KEY"));
  }

  /***
   * Remove comments from a SQL statement.
   *
   * @param sqlStmt The SQL statement to adjust.
   * @return The adjusted SQL statement.
   */
  private static String removeComments(String sqlStmt) {
    sqlStmt = sqlStmt.replaceAll("--.*", "");
    sqlStmt = sqlStmt.replaceAll("\\r\\n\\r\\n", "\n");

    return sqlStmt;
  }

  /***
   * Add a DROP TABLE IF EXISTS statement to the list of SQL statements to run.
   * <p>
   * 1. Method Purpose:
   *    This method is designed to add a "DROP TABLE IF EXISTS" statement to a list of SQL statements that will be executed. It does this for tables that are being created in the input SQL statement.
   * <p>
   * 2. Parameters:
   *    -  `sqlStmt` : A String containing the SQL statement to be analyzed.
   *    -  `finalSqlListToRun` : A List of Strings that will store the SQL statements to be executed.
   * <p>
   * 3. Regular Expression:
   *    - The method uses a regular expression to parse the input SQL statement.
   *    - It looks for a pattern that matches "CREATE TABLE IF NOT EXISTS" followed by the table name.
   *    - The table name is captured in a group for later use.
   * <p>
   * 4. Parsing the SQL:
   *    - The  `getRegExGroupMatch`  method (not shown in this snippet) is called to extract the table name from the SQL statement using the defined regular expression.
   * <p>
   * 5. Adding the DROP statement:
   *    - If a table name is successfully extracted (i.e.,  `tableName`  is not null), the method adds a new SQL statement to  `finalSqlListToRun` .
   *    - The new statement is "DROP TABLE IF EXISTS [tableName] CASCADE".
   *    - This ensures that if the table already exists, it will be dropped before the CREATE TABLE statement is executed.
   * <p>
   * 6. CASCADE option:
   *    - The CASCADE option is added to the DROP statement, which means that any dependent objects (like views or foreign key constraints) will also be dropped along with the table.
   * <p>
   * In summary, this method is part of a SQL execution preparation process. It ensures that for each "CREATE TABLE IF NOT EXISTS" statement, there's a corresponding "DROP TABLE IF EXISTS" statement added before it. This approach allows for idempotent table creation, meaning the script can be run multiple times without error, always resulting in a fresh table creation.
   *
   * @param sqlStmt The SQL statement to adjust.
   * @param finalSqlListToRun The list of SQL statements to run.
   */
  private static void addDropTableIfExistsStatement(
      String sqlStmt, List<String> finalSqlListToRun) {
    Pattern createTableParseRegExPattern =
        Pattern.compile("CREATE\\s*TABLE\\s*IF\\s*NOT\\sEXISTS\\s*([A-Za-z0-9_\\.]*)\\s.*\\s");

    String tableName = getRegExGroupMatch(sqlStmt, createTableParseRegExPattern, 1);
    if (tableName != null) finalSqlListToRun.add("DROP TABLE IF EXISTS " + tableName + " CASCADE");
  }

  /***
   * Break an ALTER statement into smaller pieces.  This might result into the
   * ALTER being broken out into multiple statements.
   * <p>
   * This code is a Java method that breaks down an ALTER TABLE SQL statement into
   * smaller, more manageable pieces. Here's a breakdown of what the code does:
   * <p>
   * 1. It defines two regular expression patterns:
   *    - One to match the table name in an ALTER TABLE statement
   *    - Another to match column names in an ALTER COLUMN statement
   * <p>
   * 2. It extracts the table name from the input SQL statement using the first regex pattern.
   * <p>
   * 3. If a table name is found, it removes the "ALTER TABLE xxx" prefix from the statement.
   * <p>
   * 4. It then splits the remaining statement into individual ALTER clauses.
   * <p>
   * 5. For each ALTER clause:
   *    - It checks if the clause is supported in H2 database (using a separate method not shown here).
   *    - It replaces certain keywords to make them compatible with H2 syntax.
   *    - It handles special cases, such as when a column is both renamed and altered in the same statement.
   *    - It removes the "UNSIGNED" keyword for BIGINT data types.
   * <p>
   * 6. Finally, it reconstructs each ALTER clause into a complete SQL statement
   *    and adds it to the finalSqlListToRun list.
   * <p>
   * The purpose of this method appears to be adapting ALTER TABLE statements from one SQL dialect (possibly MySQL) to be compatible with H2 database syntax. It breaks down complex ALTER statements into simpler ones that H2 can understand and execute.
   * <p>
   * Key points:
   * - It uses regular expressions for parsing.
   * - It handles various SQL syntax differences between MariaDB/MySql and H2.
   * - It can split a single complex ALTER statement into multiple simpler statements.
   *
   * @param sqlStmt The SQL statement to adjust.
   * @param finalSqlListToRun The list of SQL statements to run.
   */
  private static void breakAlterIntoSmallerPieces(String sqlStmt, List<String> finalSqlListToRun) {
    Pattern alterParseRegExPattern = Pattern.compile("ALTER\\s*TABLE\\s*([A-Za-z0-9_\\.]*)\\s");
    Pattern alterParseRegExColNames =
        Pattern.compile("ALTER\\s*COLUMN\\s+([A-Za-z0-9_\\.]{1,})\\s{1,}([A-Za-z0-9_\\.]*)\\s*");

    String tableName = getRegExGroupMatch(sqlStmt, alterParseRegExPattern, 1);
    if (tableName != null) {
      // Remove the "ALTER TABLE xxx" prefix in prep for further processing,
      String stmtSansAlter = sqlStmt.replaceAll("ALTER\\s*TABLE\\s*[A-Za-z0-9_\\.]*\\s", "");
      stmtSansAlter = stmtSansAlter.trim();

      // Break ALTER segment into individual statements.
      String[] alterClauseList = stmtSansAlter.split(",");

      for (String alterClause : alterClauseList) {
        // Some ALTER things we'll just skip on H2.
        if (!doWeSupportAlterStatementInH2(alterClause)) {
          continue;
        }

        // Keyword differences.
        alterClause = alterClause.endsWith(" ") ? alterClause : alterClause + " ";

        alterClause = alterClause.replaceAll("ADD COLUMN ", "ADD ");
        alterClause = alterClause.replaceAll("CHANGE COLUMN ", "ALTER COLUMN ");
        alterClause = alterClause.replaceAll("DROP FOREIGN KEY ", "DROP CONSTRAINT ");

        // Handle case where the ALTER both renames the column and changes it at thew same time.
        if (alterClause.contains("ALTER COLUMN")) {
          // A rename-only ALTER will only contain 2 tokens ("ALTER COLUMN col1 col2").
          int elemCount = alterClause.split("\\s{1,}").length;

          if (elemCount > 4) { // Changes data types as well.
            String firstColName = getRegExGroupMatch(alterClause, alterParseRegExColNames, 1);
            String secondColName = getRegExGroupMatch(alterClause, alterParseRegExColNames, 2);

            // If we have both column names, we can do a rename.
            if ((firstColName != null) && (secondColName != null)) {
              finalSqlListToRun.add(
                  String.format(
                      "ALTER TABLE %s ALTER COLUMN %s RENAME TO %s",
                      tableName, firstColName, secondColName));

              // Also, change the data type on the second field.
              finalSqlListToRun.add(
                  String.format(
                      "ALTER TABLE %s %s", tableName, alterClause.replaceAll(firstColName, "")));

              continue;
            }
          }
        }

        // Semantic differences.
        if (alterClause.contains("BIGINT")) {
          alterClause = alterClause.replaceAll("UNSIGNED", "");
        }

        String alterSql = "ALTER TABLE " + tableName + " " + alterClause;
        finalSqlListToRun.add(alterSql);
      }
    }
  }

  /***
   * Get the first group match from a RegEx pattern.
   *
   * @param input The input string to parse.
   * @param pattern The RegEx pattern to use.
   * @param desiredGroup The desired group to return.
   * @return The first group match, or null if none found.
   */
  private static String getRegExGroupMatch(String input, Pattern pattern, int desiredGroup) {
    Matcher m = pattern.matcher(input);
    if (m.find() && m.groupCount() >= desiredGroup) {
        return m.group(desiredGroup);
      }


    return null;
  }

  /***
   * Construct the test database for our tests.  If it already exists, it will be
   * dropped and recreated freshly.
   * <p>
   * The purpose of this method is to set up a fresh, config-free H2 test database
   * using existing Flyway scripts:
   * <p>
   *    - Clearing any existing schema
   *    - Running all Flyway migration scripts in order
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
   * 4. It calls `getFileList()` to retrieve a list of Flyway migration scripts from
   *    the `FLYWAY_SCRIPT_PATH` .
   * <p>
   * 5. The script files are then sorted using `sortFlywayScriptsByVersion()`, which
   *    orders them based on their version numbers (e.g., V1.0__, V1.1__, etc.).
   * <p>
   * 6. The method sets the schema to PUBLIC.
   * <p>
   * 7. It calls  `runFlywayScriptsOnH2()`  to execute the sorted Flyway scripts on an
   *    H2 database.
   * <p>
   * 8. Finally, it sets `hasDbBeenCreatedBefore` to true, indicating that the database
   *    has been created.
   * <p>
   * This approach allows for reproducible test environments by always starting with a known database state.
   */
  public static void runFlywayScripts(
      JdbcTemplate jdbcTemplate, boolean displayLog) {
    // If the database has been created before, no need to create again now.
    if (hasDbBeenCreatedBefore) {
      if (displayLog) {
        System.out.println("H2 database already created, no need to recreate");
      }

      return;
    }

    if (displayLog) {
      System.out.println("\n---[ Creating fresh test database schema ]---");
    }

    // Get a list of Flyway scripts and sort them by "Vnn.mm__". prefix.
    File[] fileList = getFileList(FLYWAY_SCRIPT_PATH);
    File[] fileListSorted = sortFlywayScriptsByVersion(fileList);

    // FINALLY - run the scripts!!!   Yabba Dabba Dooo!!!
    runFlywayScriptsOnH2(jdbcTemplate, fileListSorted, displayLog);

    hasDbBeenCreatedBefore = true;
  }

  /**
   * Runs Flyway scripts on an object annotated with @ChimpWired.
   * This method sets up an in-memory H2 database, injects DAOs into fields
   * annotated with @ChimpDao, and executes Flyway migration scripts.
   *
   * @param object The object to process. Must be annotated with @ChimpWired.
   * @param displayLog A boolean flag to control logging during script execution.
   * @throws IllegalArgumentException If the object is not properly annotated,
   *         if there are issues with DAO instantiation, or if field injection fails.
   */
  public static void chimpifyObject(Object object, boolean displayLog) {
    // Get the datasource we'll use to write to H2.  The annotated DAOs will also
    // be tied to this data source.  For now, we'll use the default H2 credentials.
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:tmp;DB_CLOSE_DELAY=-1");
    dataSource.setUsername("sa");
    dataSource.setPassword("");

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

      // Run the Flyway scripts.
    // Look for fields annotated with @ChimpDao.
    for (Field field : object.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(ChimpDao.class)) {
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

    // Run the Flyway scripts.
    runFlywayScripts(jdbcTemplate, displayLog);
  }
}
