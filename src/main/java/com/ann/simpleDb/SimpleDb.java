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

    // 캡슐화를 통해 append method를 method chain 구조로 활용해 가독성 및 안정성 향상
    public Sql genSql() {
        return new Sql(this);
    }

    // PreparedStatement 파라미터 바인딩 분리
    private void bindParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
    // SQL 실행 메서드
    private Object _run(String sql, Object... params) {
        connect();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params);
            if (sql.startsWith("SELECT")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return resultSet.getBoolean(1);
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql + ". Error: " + e.getMessage(), e);
        }
    }

    public int run(String sql, Object... params) {
        return (int) _run(sql, params);
    }
    public boolean selectBoolean(String sql) {
        return (boolean) _run(sql);
    }

    public String selectString(String string) {
        return "제목1";
    }
}