# Overview
This simple-to-use facility, `FlywayChimp`, is designed to magnify the power of unit testing by 
allowing tests to run real SQL in place of only mocking.  This makes it possible to test the SQL itself, plus ancillary things like result set field mapping and key generation, instead of just the thin DAO code. Without this ability unit tests are mocking and verifying against the same fake values, making the tests unscientific because they are unfalsifiable. 

## Flyway Scripts 
The SQL scripts are executed against an embedded H2 database in a manner that is mostly transparent to testing code. Here's a high-level explanation of the code:

The `FlywayChimp` class provides utility methods to run Flyway migration scripts on an H2 database to create a fresh schema for unit tests. This allows tests to run against a known database state without needing a separate set of scripts specifically for H2.

# Flyway Scripts Handling
1. File Retrieval: The method  `getFileList`  retrieves a list of migration script files from a specified resource directory.
*Sorting 
2. The  `sortFlywayScriptsByVersion`  method sorts these scripts numerically by their version, extracted from the filename (e.g.,  `V1.0__script.sql` ).

3. Running Scripts:
    - Main Execution: The core method  `runFlywayScriptsOnH2`  processes each script, adjusting SQL syntax where necessary to accommodate H2's limitations compared to MariaDB.
    - Adjustments: The method makes several adjustments to SQL statements, such as:
        - Modifying primary key syntax.
        - Removing unsupported features like triggers and stored procedures.
        - Adjusting  `CREATE TABLE`  and  `ALTER TABLE`  statements to be compatible with H2.
        - Simplifying SQL by removing comments and unsupported syntax.

4. Error Handling: Errors encountered during SQL execution are logged but do not stop the process. This approach allows the script to continue running even if some statements fail.

5. Utility Methods:
    - Several private methods assist in SQL transformation, such as  `adjustPrimaryKeySyntax` ,  `removeIndexes` , and  `breakAlterIntoSmallerPieces` .

6. Running on Fresh Database: The  `runFlywayScripts`  method ensures a clean database state by dropping and recreating the schema before running the scripts. It uses the sorted list of scripts and executes them in order.

7. Logging: The class provides optional logging of script execution details, which can be toggled via a method parameter. This logging includes summaries of executed SQL statements, skipped statements, and any errors encountered.

Overall, this utility class is designed to streamline the process of setting up a test database environment by automating the execution and adaptation of Flyway migration scripts for use with an H2 database.

## Usage

The following code snippet shows how to use the utility class to run Flyway scripts on a fresh H2 database.  This example is a JUnit test class that is testing DAOs.  

```java
class MyDaoTests {

    private JdbcTemplate jdbcTemplate;
    private MyDao myDao;

    // This method is the only part different from a normal test.  It's a little more complicated because autowiring doesn't work in this case.
    public MyDaoTests() {
        this.myDao = (MyDao)
            ConfigForTests.createDaoPossiblyLinkedToH2(MyDao.class);

        FlywayChimp.runFlywayScripts(
                this.myDao.jdbcTemplate, true);
    }

    @Test
    void testInsert() {
        MyRecord myRec = new MyRecord();
        myRec.setName("Test Name");
        myRec.setDescription("Test Description");
        myRec.setXXX(...0);

        // Insert!
        this.myDao.insert(myRec);

        // Verify.
        assertEquals(1, myRec.getId());
    }
}
```

## Advantages
* The test itself does not require mocking and thus is extremely simple.
* It tests an actually functioning DAO that uses its SQL.
* Things such as SQL grammar issues will be detected.
* Uses the same Flyway scripts as the production database.

## Disadvantages
* It is a bit more complicated to set up.
* There is small possibly of script failures or H2 syntax translation issues while applying the Flyway scripts on H2. These could cause a very small number of tests types to not work, though this will show up during test development.  In this case mocks would be required for that test.

## Settings
The following settings are available to customize the utility class.
* ``nrm.db.tests.useFlywayChimp=true`` enables/disables the use of Flyway scripts on H2.
* ``nrm.db.tests.flyway.scripts.path`` sets the path to the Flyway scripts.  When enabled it performs the activity described in this document.  When disabled, H2 is not used and everything goes through the actual DB.