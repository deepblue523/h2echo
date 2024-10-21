package com.deepblue.h2echo.dialects;

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
public class MariaDbDialectRunner extends MariaDbMySqlCommonRunner {
}
