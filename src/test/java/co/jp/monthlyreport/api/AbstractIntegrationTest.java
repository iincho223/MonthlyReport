package co.jp.monthlyreport.api;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 全 Controller テストの基底クラス。
 * Testcontainers で起動した MariaDB に対して Flyway が V1/V2 マイグレーションを自動適用するため、
 * サブクラスは InMemoryDataStore 時代と同じ初期データ（EMP001〜EMP010 等）を前提にできる。
 * コンテナはテストクラス間で使い回し、起動コストを抑える（static フィールド + JVM 終了時に破棄）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

  @Container
  @ServiceConnection
  static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>("mariadb:11.4");
}
