package org.jujubeframework.util.security;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <pre>
 * 签名相关工具类.
 * Created by Binary Wang on 2017-3-23.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">binarywang(Binary Wang)</a>
 */
@Slf4j
public class SignUtil {

    /**
     * 微信支付签名算法(详见:https://pay.weixin.qq.com/wiki/doc/api/tools/cash_coupon.php?chapter=4_3).
     *
     * @param params
     *            参数信息
     * @param signType
     *            签名类型，如果为空，则默认为MD5
     * @param signKey
     *            签名Key
     * @param ignoredParams
     *            签名时需要忽略的特殊参数
     * @return 签名字符串 string
     */
    public static String createSign(Map<String, String> params, SignType signType, String signKey, String[] ignoredParams) {
        SortedMap<String, String> sortedMap = new TreeMap<>(params);

        StringBuilder toSign = new StringBuilder();
        for (String key : sortedMap.keySet()) {
            String value = params.get(key);
            boolean shouldSign = false;
            if (StringUtils.isNotEmpty(value) && !ArrayUtils.contains(ignoredParams, key)
                    && !Lists.newArrayList("sign", "key", "xmlString", "xmlDoc", "couponList").contains(key)) {
                shouldSign = true;
            }

            if (shouldSign) {
                toSign.append(key).append("=").append(value).append("&");
            }
        }

        toSign.append("key=").append(signKey);
        if (SignType.HMAC_SHA256.equals(signType)) {
            return createHmacSha256Sign(toSign.toString(), signKey);
        } else {
            return DigestUtils.md5Hex(toSign.toString()).toUpperCase();
        }
    }

    /**
     * HmacSHA256 签名算法
     *
     * @param message
     *            签名数据
     * @param key
     *            签名密钥
     */
    public static String createHmacSha256Sign(String message, String key) {
        try {
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256.init(secretKeySpec);
            byte[] bytes = sha256.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(bytes).toUpperCase();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            SignUtil.log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 校验签名是否正确.
     *
     * @param params
     *            需要校验的参数Map
     * @param signType
     *            签名类型，如果为空，则默认为MD5
     * @param signKey
     *            校验的签名Key
     * @return true - 签名校验成功，false - 签名校验失败
     */
    public static boolean checkSign(Map<String, String> params, SignType signType, String signKey) {
        String sign = createSign(params, signType, signKey, new String[0]);
        return sign.equals(params.get("sign"));
    }

    /**
     * 签名类型.
     */
    public enum SignType {
        /**
         * The constant HMAC_SHA256.
         */
        HMAC_SHA256,
        /**
         * The constant MD5.
         */
        MD5

    }

}
