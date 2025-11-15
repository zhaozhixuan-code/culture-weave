package com.zzx.cultureweavebackend.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Kryo 序列化工具（使用 writeClassAndObject/readClassAndObject）
 */
public class KryoSerializationUtil {

    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 对象序列化为 Base64 字符串（使用 writeClassAndObject，包含类型信息）
     */
    public static String serializeToBase64(Object obj) {
        if (obj == null) {
            return null;
        }

        // Kryo kryo = kryoLocal.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 推荐使用 try-with-resources 以便自动 close Output（会 flush 写入 baos）
        try (Output output = new Output(baos)) {
            // 使用 writeClassAndObject 写入类型信息，反序列化时无需显式类
            kryo.writeClassAndObject(output, obj);
            // close() 会 flush 到 baos，try-with-resources 会自动调用
        } catch (Exception e) {
            throw new RuntimeException("Kryo serialization failed", e);
        }

        byte[] bytes = baos.toByteArray(); // 现在肯定有数据
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Base64 字符串反序列化为对象（对应 writeClassAndObject）
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        byte[] bytes = Base64.getDecoder().decode(base64);
        // Kryo kryo = kryoLocal.get();
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            Object o = kryo.readClassAndObject(input);
            return (T) o;
        } catch (Exception e) {
            throw new RuntimeException("Kryo deserialization failed", e);
        }
    }

    // 可选：用于调试，返回 Base64 解码后字节长度
    public static int debugBase64Length(String base64) {
        if (base64 == null) return 0;
        return Base64.getDecoder().decode(base64).length;
    }
}
