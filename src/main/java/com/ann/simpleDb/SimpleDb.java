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
    private <T> T _run(String sql, Class<?> cls, Object... params) {
        connect(); // 데이터베이스 연결 초기화
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameters(preparedStatement, params); // 파라미터 바인딩
            // SELECT 쿼리인지 확인하여 처리
            if (sql.startsWith("SELECT")) {
                ResultSet resultSet = preparedStatement.executeQuery(); // SELECT 쿼리 실행
                resultSet.next(); // 첫 번째 결과로 이동

                if (cls == String.class) {
                    return (T) resultSet.getString(1);
                } else if (cls == Boolean.class) {
                    return (T) (Boolean) resultSet.getBoolean(1); // 첫 번째 컬럼 값을 반환
                }
            }
            // INSERT, UPDATE, DELETE 쿼리는 실행 후 영향을 받은 행 수 반환
            return (T) (Integer) preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql + ". Error: " + e.getMessage(), e);
        }
    }

    // SQL 실행 메서드 (INSERT, UPDATE, DELETE) - 영향을 받은 행 수 반환
    public int run(String sql, Object... params) {
        return _run(sql, Integer.class, params);
    }

    // SELECT 쿼리 실행 메서드 - Boolean 값 반환
    public boolean selectBoolean(String sql) {
        return _run(sql, Boolean.class);
    }

    // SELECT 쿼리 실행 메서드 - String 값 반환
    public String selectString(String sql) {
        return _run(sql, String.class);
    }
}