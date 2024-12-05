package com.ann.simpleDb;

import lombok.RequiredArgsConstructor;

// final field의 생성자 자동으로 생성
@RequiredArgsConstructor
public class SimpleDb {
    private final String host;
    private final String username;
    private final String password;
    private final String dbName;
}
