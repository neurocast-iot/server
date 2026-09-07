package com.neurocast.framework.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * FastJson2 全局序列化器注册（与原项目语义一致）：
 * - OffsetDateTime <-> 秒级时间戳
 * - LocalDateTime  <-> 毫秒级时间戳
 * - Date           <-> 毫秒级时间戳
 * - BigDecimal/BigInteger 兼容字符串与数字输入
 */
public final class FastJsonCodecs {

    private FastJsonCodecs() {
    }

    /**
     * 注册全部自定义编解码器（进程级全局生效，重复调用无副作用）
     */
    public static void registerAll() {
        JSON.register(OffsetDateTime.class, OFFSET_DATE_TIME_WRITER);
        JSON.register(OffsetDateTime.class, OFFSET_DATE_TIME_READER);

        JSON.register(LocalDateTime.class, LOCAL_DATE_TIME_WRITER);
        JSON.register(LocalDateTime.class, LOCAL_DATE_TIME_READER);

        JSON.register(Date.class, DATE_WRITER);
        JSON.register(Date.class, DATE_READER);

        JSON.register(BigDecimal.class, BIG_DECIMAL_WRITER);
        JSON.register(BigDecimal.class, BIG_DECIMAL_READER);

        JSON.register(BigInteger.class, BIG_INTEGER_WRITER);
        JSON.register(BigInteger.class, BIG_INTEGER_READER);
    }

    /**
     * OffsetDateTime -> 秒级时间戳
     */
    private static final ObjectWriter<OffsetDateTime> OFFSET_DATE_TIME_WRITER = (jsonWriter, object, fieldName, fieldType, features) -> {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeInt64(((OffsetDateTime) object).toEpochSecond());
        }
    };

    /**
     * 秒级时间戳 -> OffsetDateTime（UTC）
     */
    private static final ObjectReader<OffsetDateTime> OFFSET_DATE_TIME_READER = (jsonReader, fieldType, fieldName, features) -> {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(jsonReader.readInt64Value()), ZoneOffset.UTC);
    };

    /**
     * LocalDateTime -> 毫秒级时间戳
     */
    private static final ObjectWriter<LocalDateTime> LOCAL_DATE_TIME_WRITER = (jsonWriter, object, fieldName, fieldType, features) -> {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeInt64(((LocalDateTime) object).toInstant(ZoneOffset.UTC).toEpochMilli());
        }
    };

    /**
     * 毫秒级时间戳 -> LocalDateTime（UTC）
     */
    private static final ObjectReader<LocalDateTime> LOCAL_DATE_TIME_READER = (jsonReader, fieldType, fieldName, features) -> {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonReader.readInt64Value()), ZoneOffset.UTC);
    };

    /**
     * Date -> 毫秒级时间戳
     */
    private static final ObjectWriter<Date> DATE_WRITER = (jsonWriter, object, fieldName, fieldType, features) -> {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeInt64(((Date) object).getTime());
        }
    };

    /**
     * 毫秒级时间戳 -> Date
     */
    private static final ObjectReader<Date> DATE_READER = (jsonReader, fieldType, fieldName, features) -> {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        return new Date(jsonReader.readInt64Value());
    };

    /**
     * BigDecimal 原样输出
     */
    private static final ObjectWriter<BigDecimal> BIG_DECIMAL_WRITER = (jsonWriter, object, fieldName, fieldType, features) -> {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeDecimal((BigDecimal) object);
        }
    };

    /**
     * BigDecimal 兼容字符串/数字输入
     */
    private static final ObjectReader<BigDecimal> BIG_DECIMAL_READER = (jsonReader, fieldType, fieldName, features) -> {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        String value = jsonReader.readString();
        return value == null || value.isEmpty() ? null : new BigDecimal(value);
    };

    /**
     * BigInteger 原样输出
     */
    private static final ObjectWriter<BigInteger> BIG_INTEGER_WRITER = (jsonWriter, object, fieldName, fieldType, features) -> {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeString(((BigInteger) object).toString());
        }
    };

    /**
     * BigInteger 兼容字符串/数字输入
     */
    private static final ObjectReader<BigInteger> BIG_INTEGER_READER = (jsonReader, fieldType, fieldName, features) -> {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        String value = jsonReader.readString();
        return value == null || value.isEmpty() ? null : new BigInteger(value);
    };
}
