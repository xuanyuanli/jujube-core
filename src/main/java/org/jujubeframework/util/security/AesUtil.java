package org.jujubeframework.util.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES算法（加密、解密工具类）
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesUtil {
    private static final Logger logger = LoggerFactory.getLogger(AesUtil.class);

    public static final String DEFAULT_ENCRYPT_KEY = "XXCYW-AES";

    private static final String IVKEY = "AESAPPCLIENT_KEY";

    /**
     * 对数据加密
     *
     * @param data 被加密的数据
     * @param key  密钥
     */
    public static String encrypt(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            SecretKeySpec keyspec = new SecretKeySpec(fullZore(key, blockSize), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(fullZore(IVKEY, blockSize));
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(fullZore(data, blockSize));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            logger.error("aes", e);
            return "";
        }
    }

    /**
     * 对数据解密
     *
     * @param data 被解密的数据
     * @param key  密钥
     */
    public static String decrypt(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            SecretKeySpec keyspec = new SecretKeySpec(fullZore(key, blockSize), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(fullZore(IVKEY, blockSize));
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(data));
            return new String(decrypted).trim();
        } catch (Exception e) {
            logger.error("aes", e);
            return "";
        }
    }

    private static byte[] fullZore(String data, int blockSize) {
        byte[] dataBytes = data.getBytes();
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
        }
        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
        return plaintext;
    }

}
