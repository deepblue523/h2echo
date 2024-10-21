package com.deepblue.h2echo.dialects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DialectRunnerBase {
    // Used to track which script have already been run.  Running SQL is
    // idempotent - can be run multiple times and only newly queued ones will
    // actually be executed against H2.
    private static List<String> scriptsAlreadyExecuted = new ArrayList<>();

    /***
     * Remove comments from a SQL statement.
     *
     * @param sqlStmt The SQL statement to adjust.
     * @return The adjusted SQL statement.
     */
    protected String removeComments(String sqlStmt) {
        sqlStmt = sqlStmt.replaceAll("--.*", "");
        sqlStmt = sqlStmt.replaceAll("\\r\\n\\r\\n", "\n");

        return sqlStmt;
    }


    /***
     * Determine if we support a given statement in H2 and need in our tests.
     * Supporting fewer features give fewer things to fail.
     *
     * @param stmt The SQL statement to check.
     * @return True if we support it, false if not.
     */
    protected boolean doWeSupportStatementInH2(String stmt) {
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
    protected boolean doWeSupportAlterStatementInH2(String stmt) {
        return (stmt.contains("ADD COLUMN")
                || stmt.contains("CHANGE COLUMN")
                || stmt.contains("RENAME TO")
                || stmt.contains("DROP PRIMARY KEY")
                || stmt.contains("ADD PRIMARY KEY")
                || stmt.contains("DROP FOREIGN KEY"));
    }

    /***
     * Get the first group match from a RegEx pattern.
     *
     * @param input The input string to parse.
     * @param pattern The RegEx pattern to use.
     * @param desiredGroup The desired group to return.
     * @return The first group match, or null if none found.
     */
    protected String getRegExGroupMatch(String input, Pattern pattern, int desiredGroup) {
        Matcher m = pattern.matcher(input);
        if (m.find() && m.groupCount() >= desiredGroup) {
            return m.group(desiredGroup);
        }

        return null;
    }

    protected void makeStatementAdjustmentsForSyntax(
            String originalStmtToRunAdj, List<String> finalSqlListToRun) {
        // Nothing to do for DBs like H2 (it is a 1->1 statement transfer).
    }

    /***
     * Run SQL scripts on an H2 database.  This is a bit of a hack, but it's
     * necessary because H2 doesn't support all the SQL syntax that MariaDB
     * does.  So we need to make some adjustments to the SQL before running it.
     * <p>
     * The nice thing about this method is that it is very  to use.  There is no
     * setup - secrets, variables, nothing.  It just runs.
     * <p>
     * Given a list of SQL scripts, this method does the following types of things:
     * <p>
     *    * Iterates through each SQL script file, reads its contents, and processes
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
    public void runSqlScriptsOnH2(
            JdbcTemplate jdbcTemplate, File[] fileList, boolean displayLog) {
        // Load each script.
        int totalStmtsSkipped = 0;
        int totalStmtsRun = 0;

        List<String> errorLogList = new ArrayList<String>();
        int totalErrors = 0;

        // ---[ Look at each SQL script file ]---
        // At this point this should be sorted by version.
        for (File file : fileList) {
            // Let's not execute the same scri[t more than once.
            if (scriptsAlreadyExecuted.contains(file.getName())) {
                if (displayLog) {
                    System.out.println("Script already run on H2, not re-running: " + file.getName());
                }

            }

            scriptsAlreadyExecuted.add(file.getName());
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

            // ---[ Split into individual SQL statements from SQL script]---
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

                // Make statement adjustments as appropriate.
                makeStatementAdjustmentsForSyntax(originalStmtToRunAdj, finalSqlListToRun);

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
}
