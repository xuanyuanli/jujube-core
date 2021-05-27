package org.jujubeframework.support;

import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Dates;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RecordTest {

    @Test
    public void getStr() {
        Record record = new Record().set("key", "abc");
        assertThat(record.getStr("key")).isEqualTo("abc");
    }

    @Test
    public void getInt() {
        Record record = new Record().set("key", "123");
        assertThat(record.getInt("key")).isEqualTo(123);
    }

    @Test
    public void getLong() {
        Record record = new Record().set("key", "123");
        assertThat(record.getLong("key")).isEqualTo(123L);
    }

    @Test
    public void getBigInteger() {
        Record record = new Record().set("key", BigInteger.valueOf(123));
        assertThat(record.getBigInteger("key")).isEqualTo(123).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
    }

    @Test
    public void getDate() {
        Record record = new Record().set("key", Dates.parse("2017-06-28"));
        assertThat(record.getDate("key")).isEqualTo("2017-06-28").isAfter("2017-06-27").isBefore("2017-06-29");
    }

    @Test
    public void getDouble() {
        Record record = new Record().set("key", "123.456");
        assertThat(record.getDouble("key")).isEqualTo(123.456);
    }

    @Test
    public void getFloat() {
        Record record = new Record().set("key", "123.456");
        assertThat(record.getFloat("key")).isEqualTo(123.456f);
    }

    @Test
    public void getBoolean() {
        Record record = new Record().set("key", "true");
        assertThat(record.getBoolean("key")).isEqualTo(true);
    }

    @Test
    public void getBigDecimal() {
        Record record = new Record().set("key", new BigDecimal(1.22));
        assertThat(record.getBigDecimal("key")).isEqualTo(new BigDecimal(1.22));
    }

    @Test
    public void getBytes() {
        Record record = new Record().set("key", "123".getBytes());
        assertThat(record.getBytes("key")).isEqualTo("123".getBytes());
    }

    @Test
    public void getNumber() {
        Record record = new Record().set("key", new BigDecimal(1.22));
        assertThat(record.getNumber("key")).isEqualTo(new BigDecimal(1.22));
    }

    @Test
    public void getId() {
        Record record = new Record().set("id", "123");
        assertThat(record.getId()).isEqualTo(123);
    }

    @Test
    public void valueOf() {
        Record record = new Record().set("id", "123");
    }

    @Test
    public void getRecord() {
        Record record = new Record();
        Record record2 = new Record();
        record.set("key", record2);
        assertThat(record.getRecord("key")).isEqualTo(record2);
    }

    @Test
    public void getList() {
    }

}
