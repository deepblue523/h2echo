# Overview
This simple-to-use facility, `H2Echo`, is designed to magnify the power of unit testing by 
allowing tests to run real SQL in place of only mocking.  This makes it possible to test the SQL itself, plus ancillary things like result set field mapping and key generation, instead of just the thin DAO code. Without this ability unit tests are mocking and verifying against the same fake values, making the tests unscientific because they are unfalsifiable. 

## SQL Scripts 
The SQL scripts are executed against an embedded H2 database in a manner that is mostly transparent to testing code. Here's a high-level explanation of the code:

The `H2Echo` class provides utility methods to run SQL scripts on an H2 database to create a fresh schema for unit tests. This allows tests to run against a known database state without needing a separate set of scripts specifically for H2.

# SQL Scripts Handling
1.File Retrieval: The method  `getFileList`  retrieves a list of SQL script files from a specified resource directory.

2. The  `sortSqlScriptsByVersion`  method sorts these scripts numerically by their version, extracted from the filename (e.g.,  `V1.0__script.sql` ).  This sorting scheme is compatible with Flyway's default behavior, but may be extended later to support other schemes.

3. Running Scripts:
    - Main Execution: The core method  `runSqlScriptsOnH2`  processes each script, adjusting SQL syntax where necessary to accommodate H2's limitations compared to MariaDB.
    - Adjustments: The method makes several adjustments to SQL statements, such as:
        - Modifying primary key syntax.
        - Removing unsupported features like triggers and stored procedures.
        - Adjusting  `CREATE TABLE`  and  `ALTER TABLE`  statements to be compatible with H2.
        - Simplifying SQL by removing comments and unsupported syntax.

4. Error Handling: Errors encountered during SQL execution are logged but do not stop the process. This approach allows the script to continue running even if some statements fail.

5. Utility Methods:
    - Several private methods assist in SQL transformation, such as  `adjustPrimaryKeySyntax` ,  `removeIndexes` , and  `breakAlterIntoSmallerPieces`.  These adjustments are specific to
      MariaDB -> H2, and others can be added in the future.

6. Running on Fresh Database: The  `runFlywayScripts`  is idempotent, meaning it can be run multiple times without causing problems.  It will skip any scripts that have already been run and run new ones.~~~~

7. Logging: The class provides optional logging of script execution details, which can be toggled via a method parameter. This logging includes summaries of executed SQL statements, skipped statements, and any errors encountered.

Overall, this utility class is designed to streamline the process of setting up a test database environment by automating the execution and adaptation of SQL SQL scripts for use with an H2 database.

## Usage~~~~

The following code snippet shows how to use the utility class to run SQL scripts on a fresh H2 database.  This example is a JUnit test class that is testing DAOs.  

```java
// Things like the script path can be specified if they deviate from the default.
@EnableH2Echo
class MyDaoTests {

    // Defaults can be overridden on annotations here as well.
    @EchoDao
    private MyDao myDao;

    public MyDaoTests() {
       // Does stuff based upon annotations.
       echoDaosOnObject(this, true); 
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
* The test code looks exactly like actual Production code that would use the DAOs.
* It tests an actually functioning DAO that uses its SQL.
* Things such as SQL grammar issues will be detected with some limitations.
* Uses the same SQL scripts as the production database, with some modifications.
* Enables black box testing for any DAO, bypassing any need to adjust DAOs themselves~~~~ for testing.

## Disadvantages
There is small possibly of script failures or H2 syntax translation issues while applying the SQL scripts on H2. These could cause a very small number of tests types to not work, though this will show up during test development.  In this case mocks would be required for that test.
