package org.jujubeframework.support;

import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Dates;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RecordTest {

    @Test
    public void testGetStr() {
        Record record = new Record().set("key", "abc");
        assertThat(record.getStr("key")).isEqualTo("abc");
    }

    @Test
    public void testGetInt() {
        Record record = new Record().set("key", "123");
        assertThat(record.getInt("key")).isEqualTo(123);
    }

    @Test
    public void testGetLong() {
        Record record = new Record().set("key", "123");
        assertThat(record.getLong("key")).isEqualTo(123L);
    }

    @Test
    public void testGetBigInteger() {
        Record record = new Record().set("key", BigInteger.valueOf(123));
        assertThat(record.getBigInteger("key")).isEqualTo(123).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
    }

    @Test
    public void testGetDate() {
        Record record = new Record().set("key", Dates.parse("2017-06-28"));
        assertThat(record.getDate("key")).isEqualTo("2017-06-28").isAfter("2017-06-27").isBefore("2017-06-29");
    }

    @Test
    public void testGetDouble() {
        Record record = new Record().set("key", "123.456");
        assertThat(record.getDouble("key")).isEqualTo(123.456);
    }

    @Test
    public void testGetFloat() {
        Record record = new Record().set("key", "123.456");
        assertThat(record.getFloat("key")).isEqualTo(123.456f);
    }

    @Test
    public void testGetBoolean() {
        Record record = new Record().set("key", "true");
        assertThat(record.getBoolean("key")).isEqualTo(true);
    }

    @Test
    public void testGetBigDecimal() {
        Record record = new Record().set("key", new BigDecimal(1.22));
        assertThat(record.getBigDecimal("key")).isEqualTo(new BigDecimal(1.22));
    }

    @Test
    public void testGetBytes() {
        Record record = new Record().set("key", "123".getBytes());
        assertThat(record.getBytes("key")).isEqualTo("123".getBytes());
    }

    @Test
    public void testGetNumber() {

    }

    @Test
    public void testGetId() {
        Record record = new Record().set("id", "123");
        assertThat(record.getId()).isEqualTo(123);
    }

    @Test
    public void testValueOf() {
    }

    @Test
    public void testGetRecord() {
        Record record = new Record();
        Record record2 = new Record();
        record.set("key", record2);
        assertThat(record.getRecord("key")).isEqualTo(record2);
    }

    @Test
    public void testGetList() {
    }

}
