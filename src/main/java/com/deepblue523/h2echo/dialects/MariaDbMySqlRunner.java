package com.deepblue523.h2echo.dialects;

import java.util.List;
import java.util.regex.Pattern;

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
public class MariaDbMySqlRunner extends DialectRunnerBase {

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
  private void addDropTableIfExistsStatement(
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
  private void breakAlterIntoSmallerPieces(String sqlStmt, List<String> finalSqlListToRun) {
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

  protected void makeStatementAdjustmentsForSyntax(String originalStmtToRunAdj, List<String> finalSqlListToRun) {
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
  }
}
