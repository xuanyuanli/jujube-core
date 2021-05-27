package org.jujubeframework.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

/**
 * 二维码工具
 *
 * @author John Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QrCodes {

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

    /**
     * 带有logo的二维码
     * 
     * @param contents
     *            二维码内容
     * @param width
     *            宽
     * @param height
     *            高
     * @param logoImage
     *            logo图片
     * @param isCompressLogImage
     *            是否压缩logo图片到合适的尺寸
     */
    public static BufferedImage createQrCodeWithLogo(String contents, int width, int height, BufferedImage logoImage, boolean isCompressLogImage) {
        try {
            if (isCompressLogImage) {
                int newWidth = (int) (width / 6.2);
                int newHeight = (int) (height / 6.2);
                logoImage = Thumbnails.of(logoImage).size(newWidth, newHeight).asBufferedImage();
            }
            BufferedImage qrcode = encode(contents, width, height);
            int deltaHeight = height - logoImage.getHeight();
            int deltaWidth = width - logoImage.getWidth();
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) combined.getGraphics();
            g.drawImage(qrcode, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.drawImage(logoImage, (int) Math.round(deltaWidth / 2.0), (int) Math.round(deltaHeight / 2.0), null);
            return combined;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
