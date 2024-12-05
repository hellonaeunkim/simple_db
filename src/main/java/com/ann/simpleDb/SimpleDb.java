package com.ann.simpleDb;

import lombok.RequiredArgsConstructor;

import java.sql.*;

// final field의 생성자 자동으로 생성
@RequiredArgsConstructor
public class SimpleDb {
    private final String host;
    private final String username;
    private final String password;
    private final String dbName;
    private Connection connection;

    // Database 연결 초기화
    private void connect() {
        if (connection == null) {
            String url = String.format("jdbc:mysql://%s/%s?useSSL=false", host, dbName);
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to database" + e.getMessage(), e);
            }
        }
    }

    // SQL 실행 method
    public void run(String sql, Object... params) {
        connect(); // 연결 초기화

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params); // 파라미터 바인딩 로직 분리
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql + ". Error: " + e.getMessage(), e);
        }
    }

    // PreparedStatement 파라미터 바인딩 분리
    private void bindParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    // 자원 해제
    public void close() {
        if (connection == null) {
            return; // 연결이 없는 경우 아무 작업도 하지 않음
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection: " + e.getMessage(), e);
        }
    }
}
