# oracle-cursor-leak-vertx

This project contains a single test that demonstrates an alleged cursor leak when using the Vert.x reactive Oracle
drivers with Orace 18.4 (in testcontainers).

The test, in a loop, initializes a pool, grabs a connection, starts a transaction, runs a `SELECT * FROM dual;`, commits
the transaction and then closes the connection.

After a number of loop iterations (usually around 300 on the test machine) the database will
throw `ORA-01000: maximum open cursors exceeded`.

## Running

```
./gradlew test -i
```

## Example output

```
TestMainVerticle > oracle(Vertx, VertxTestContext) FAILED
    io.vertx.oracleclient.OracleException: Error : 1000, Position : 0, Sql = SET TRANSACTION ISOLATION LEVEL READ COMMITTED, OriginalSql = SET TRANSACTION ISOLATION LEVEL READ COMMITTED, Error Msg = ORA-01000: maximum open cursors exceeded
        (Coroutine boundary)
        at com.kobil.test.oracle_cursor_test.TestMainVerticle$oracle$1.invokeSuspend(TestMainVerticle.kt:55)

        Caused by:
        io.vertx.oracleclient.OracleException: Error : 1000, Position : 0, Sql = SET TRANSACTION ISOLATION LEVEL READ COMMITTED, OriginalSql = SET TRANSACTION ISOLATION LEVEL READ COMMITTED, Error Msg = ORA-01000: maximum open cursors exceeded

1 test completed, 1 failed
```

## M1 Users

Docker for Mac will use qemu because there are no ARM images of oracle-xe available. This is very slow, and Oracle fails
to start before the test times out, as tested on an M1 Max.
