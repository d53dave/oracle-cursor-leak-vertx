package com.kobil.test.oracle_cursor_test

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.oracleclient.OracleConnectOptions
import io.vertx.oracleclient.OraclePool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.containers.OracleContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ExtendWith(VertxExtension::class)
@Testcontainers
class TestMainVerticle {

  private lateinit var scope: CoroutineScope

  @Container
  val oracle: OracleContainer = OracleContainer(DockerImageName.parse("gvenzl/oracle-xe").withTag("18.4.0-slim"))

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    scope = CoroutineScope(vertx.dispatcher() + SupervisorJob())
    vertx.deployVerticle(MainVerticle(), testContext.succeeding<String> { _ -> testContext.completeNow() })
  }

  @Test
  fun oracle(vertx: Vertx, testContext: VertxTestContext) {
    val connectOptions = OracleConnectOptions()
      .setPort(oracle.oraclePort)
      .setHost(oracle.host)
      .setDatabase(oracle.databaseName)
      .setUser(oracle.username)
      .setPassword(oracle.password)

    val poolOptions = PoolOptions()
      .setMaxSize(5)

    val pool = OraclePool.pool(connectOptions, poolOptions)

    scope.launch {
      (1..1000000).forEach { run ->
        try {
          val con = pool.connection.await()
          val trx = con.begin().await()
          val res = con.query("select * from dual").execute().await()
          res.forEach { it.getString(0) }
          trx.commit().await()
          con.close().await()
        } catch (t: Throwable) {
          println("Exception caught in run $run")
          testContext.failNow(t)
          return@launch
        }
      }
    }
  }
}
