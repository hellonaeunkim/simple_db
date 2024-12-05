package com.ann.simpleDb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

// method 실행 순서 지정 : SimpleDbTest class의 모든 test method가 이름의 알파벳 순서대로 실행
@TestMethodOrder(MethodOrderer.MethodName.class)
public class SimpleDbTest {
    private static SimpleDb simpleDb;

    @BeforeAll
    public static void beforeAll() {
        // Database 정보
        simpleDb = new SimpleDb("localhost", "root", "lldj123414", "simpleDb__test");
    }
}