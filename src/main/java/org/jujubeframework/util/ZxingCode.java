package org.jujubeframework.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 利用goole的jar，生成二维码
 * @author John Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZxingCode {

    /**
     * 生成二维码
     */
    public static BufferedImage encode(String contents, int width, int height) {
        Map<EncodeHintType, Object> hints = new Hashtable<>();
        // 指定纠错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 指定编码格式
        hints.put(EncodeHintType.CHARACTER_SET, "GBK");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (Exception e) {
            log.error("encode", e);
        }
        return null;
    }

    /**
     * 生成二维码
     */
    public static InputStream encodeToInputStream(String contents, int width, int height) {
        InputStream in = null;
        ImageOutputStream outputStream;
        BufferedImage image = encode(contents, width, height);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        try {
            outputStream = ImageIO.createImageOutputStream(bs);
            ImageIO.write(image, "jpg", outputStream);
            in = new ByteArrayInputStream(bs.toByteArray());
        } catch (IOException e) {
            log.error("encode", e);
        }
        return in;
    }
}
