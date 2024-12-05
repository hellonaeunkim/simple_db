package com.ann.simpleDb;

import org.junit.jupiter.api.*;

// method 실행 순서 지정 : SimpleDbTest class의 모든 test method가 이름의 알파벳 순서대로 실행
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SimpleDbTest {
    private static SimpleDb simpleDb;

    @BeforeAll
    public static void beforeAll() {
        // Database 정보
        simpleDb = new SimpleDb("localhost", "root", "lldj123414", "simpleDb_test");

        createArticleTable();
    }

    private static void createArticleTable() {
        simpleDb.run("DROP TABLE IF EXISTS article");

        simpleDb.run("""
                CREATE TABLE article (
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    PRIMARY KEY(id),
                    createdDate DATETIME NOT NULL,
                    modifiedDate DATETIME NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    `body` TEXT NOT NULL,
                    isBlind BIT(1) NOT NULL DEFAULT 0
                )
                """);
    }

    @Test
    @DisplayName("데이터베이스 연결 테스트")
    public void t000() {
    }
}