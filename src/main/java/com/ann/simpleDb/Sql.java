package com.ann.simpleDb;

public class Sql {

    public Sql append(String sqlBit, Object... params) {
        return this;
    }

    public long insert() {
        return 1;
    }

    public int update() {
        return 3;
    }
}
