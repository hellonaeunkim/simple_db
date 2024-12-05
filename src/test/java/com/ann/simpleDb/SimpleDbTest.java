package com.ann.simpleDb;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    public void beforeEach() {
        truncateArticleTable();
        makeArticleTestData();
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

    private void truncateArticleTable() {
        simpleDb.run("TRUNCATE article");
    }

    private void makeArticleTestData() {
        IntStream.rangeClosed(1, 6).forEach(no -> {
            boolean isBlind = no > 3;
            String title = "제목%d".formatted(no);
            String body = "내용%d".formatted(no);

            simpleDb.run("""
                    INSERT INTO article
                    SET createdDate = NOW(),
                    modifiedDate = NOW(),
                    title = ?,
                    `body` = ?,
                    isBlind = ?
                    """, title, body, isBlind);
        });
    }

    @Test
    @DisplayName("insert")
    public void t001() {
        Sql sql = simpleDb.genSql();
        /*
        == rawSql ==
        INSERT INTO article
        SET createdDate = NOW() ,
        modifiedDate = NOW() ,
        title = '제목 new' ,
        body = '내용 new'
        */
        sql.append("INSERT INTO article")
            .append("SET createdDate = NOW()")
            .append(", modifiedDate = NOW()")
            .append(", title = ?", "제목 new")
            .append(", body = ?", "내용 new");

        long newId = sql.insert(); // AUTO_INCREMENT 에 의해서 생성된 주키 리턴

        assertThat(newId).isGreaterThan(0);
    }

    @Test
    @DisplayName("update")
    public void t002() {
        Sql sql = simpleDb.genSql();

        // id가 0, 1, 2, 3인 글 수정
        // id가 0인 글은 없으니, 실제로는 3개의 글이 삭제됨

        /*
        == rawSql ==
        UPDATE article
        SET title = '제목 new'
        WHERE id IN ('0', '1', '2', '3')
        */
        sql.append("UPDATE article")
            .append("SET title = ?", "제목 new")
            .append("WHERE id IN (?, ?, ?, ?)", 0, 1, 2, 3);

        // 수정된 row 개수
        int affectedRowsCount = sql.update();

        assertThat(affectedRowsCount).isEqualTo(3);
    }

    @Test
    @DisplayName("delete")
    public void t003() {
        Sql sql = simpleDb.genSql();

        // id가 0, 1, 3인 글 삭제
        // id가 0인 글은 없으니, 실제로는 2개의 글이 삭제됨
        /*
        == rawSql ==
        DELETE FROM article
        WHERE id IN ('0', '1', '3')
        */
        sql.append("DELETE")
            .append("FROM article")
            .append("WHERE id IN (?, ?, ?)", 0, 1, 3);

        // 삭제된 row 개수
        int affectedRowsCount = sql.delete();

        assertThat(affectedRowsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("selectRows")
    public void t004() {
        Sql sql = simpleDb.genSql();
        /*
        == rawSql ==
        SELECT *
        FROM article
        ORDER BY id ASC
        LIMIT 3
        */
        sql.append("SELECT * FROM article ORDER BY id ASC LIMIT 3");
        List<Map<String, Object>> articleRows = sql.selectRows();

        IntStream.range(0, articleRows.size()).forEach(i -> {
            long id = i + 1;

            Map<String, Object> articleRow = articleRows.get(i);

            assertThat(articleRow.get("id")).isEqualTo(id);
            assertThat(articleRow.get("title")).isEqualTo("제목%d".formatted(id));
            assertThat(articleRow.get("body")).isEqualTo("내용%d".formatted(id));
            assertThat(articleRow.get("createdDate")).isInstanceOf(LocalDateTime.class);
            assertThat(articleRow.get("createdDate")).isNotNull();
            assertThat(articleRow.get("modifiedDate")).isInstanceOf(LocalDateTime.class);
            assertThat(articleRow.get("modifiedDate")).isNotNull();
            assertThat(articleRow.get("isBlind")).isEqualTo(false);
        });
    }

    @Test
    @DisplayName("selectRow")
    public void t005() {
        Sql sql = simpleDb.genSql();
        /*
        == rawSql ==
        SELECT *
        FROM article
        WHERE id = 1
        */
        sql.append("SELECT * FROM article WHERE id = 1");
        Map<String, Object> articleRow = sql.selectRow();

        assertThat(articleRow.get("id")).isEqualTo(1L);
        assertThat(articleRow.get("title")).isEqualTo("제목1");
        assertThat(articleRow.get("body")).isEqualTo("내용1");
        assertThat(articleRow.get("createdDate")).isInstanceOf(LocalDateTime.class);
        assertThat(articleRow.get("createdDate")).isNotNull();
        assertThat(articleRow.get("modifiedDate")).isInstanceOf(LocalDateTime.class);
        assertThat(articleRow.get("modifiedDate")).isNotNull();
        assertThat(articleRow.get("isBlind")).isEqualTo(false);
    }

    @Test
    @DisplayName("selectDatetime")
    public void t006() {
        Sql sql = simpleDb.genSql();
        /*
        == rawSql ==
        SELECT NOW()
        */
        sql.append("SELECT NOW()");

        LocalDateTime datetime = sql.selectDatetime();

        long diff = ChronoUnit.SECONDS.between(datetime, LocalDateTime.now());

        assertThat(diff).isLessThanOrEqualTo(1L);
    }

    @Test
    @DisplayName("selectLong")
    public void t007() {
        Sql sql = simpleDb.genSql();
        /*
        == rawSql ==
        SELECT id
        FROM article
        WHERE id = 1
        */
        sql.append("SELECT id")
            .append("FROM article")
            .append("WHERE id = 1");

        Long id = sql.selectLong();

        assertThat(id).isEqualTo(1);
    }

    @Test
    @DisplayName("selectString")
    public void t008() {
        Sql sql = simpleDb.genSql();
        /*
        == rawSql ==
        SELECT title
        FROM article
        WHERE id = 1
        */
        sql.append("SELECT title")
            .append("FROM article")
            .append("WHERE id = 1");

        String title = sql.selectString();

        assertThat(title).isEqualTo("제목1");
    }

}