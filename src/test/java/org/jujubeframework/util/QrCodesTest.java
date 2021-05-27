package org.jujubeframework.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class QrCodesTest {
    public static void main(String[] args) throws IOException {
        createQrCodeWithLogo();
    }

    static void createQrCodeWithLogo() throws IOException {
        BufferedImage bufferedImage = QrCodes.createQrCodeWithLogo("https://m.artfoxlive.com/AuctionDetail?productId=" + 45230, 430, 430,
                ImageIO.read(new File("d:/data/logo-69.png")), false);
        ImageIO.write(bufferedImage, "png", new FileOutputStream("d:/data/qr-1.png"));
    }
}