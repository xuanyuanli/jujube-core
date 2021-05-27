package org.jujubeframework.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.adobe.AdobeJpegDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.icc.IccDirectory;
import io.github.biezhi.webp.WebpIO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.constant.SystemProperties;
import org.xhtmlrenderer.swing.AWTFontResolver;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 图像处理工具类
 *
 * <pre>
 * 用到开源库： imageio-jpeg(https://github.com/haraldk/TwelveMonkeys)
 * Thumbnailator(https://github.com/coobird/thumbnailator/wiki/Examples)
 * </pre>
 *
 * @author John Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Images {

    /**
     * 支持的图片格式
     */
    private static final String[] IMAGE_EXTENTIONS = { ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp", ".ico" };
    /**
     * 默认的水印透明度
     */
    private static final float DEFAULF_OPACITY = 0.45f;
    /** FSImageWriter的实例 */
    public static final FSImageWriter FS_IMAGE_WRITER = FSImageWriter.newJpegWriter(1f);

    /**
     * 等比压缩图像(原图纯压缩，不要水印)
     *
     * @param sourceFile
     *            源图像文件
     * @param destFile
     *            压缩后要存放的目标文件
     * @param maxWidth
     *            压缩后允许的最大宽度
     * @param maxHeight
     *            压缩后允许的最大高度
     */
    public static void transformOrigin(File sourceFile, File destFile, int maxWidth, int maxHeight) throws IOException {
        BufferedImage srcImage = getImage(sourceFile);
        Thumbnails.of(srcImage).size(maxWidth, maxHeight).toFile(destFile);
    }

    /**
     * 等比压缩图像(默认带水印)
     *
     * @param sourceFile
     *            源图像文件
     * @param destFile
     *            压缩后要存放的目标文件
     * @param maxWidth
     *            压缩后允许的最大宽度
     * @param maxHeight
     *            压缩后允许的最大高度
     */
    public static void transform(File sourceFile, File destFile, int maxWidth, int maxHeight, BufferedImage waterMarkImage) throws IOException {
        rotateImage(sourceFile);
        try {
            BufferedImage image = Thumbnails.of(sourceFile).scale(1).rotate(0).asBufferedImage();
            innerTransform(image, destFile, maxWidth, maxHeight, waterMarkImage, DEFAULF_OPACITY, Positions.CENTER);
        } catch (Exception e) { // 有时用常规的读取会报错，所以加下面一层
            log.error("transform常规处理出错，使用扩展图片读取器再试一遍。sourceFile:{},destFile:{}", sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
            BufferedImage image = getImage(sourceFile);
            innerTransform(image, destFile, maxWidth, maxHeight, waterMarkImage, DEFAULF_OPACITY, Positions.CENTER);
        }
    }

    /**
     * 等比压缩图像(默认带水印)
     *
     * @param sourceFile
     *            源图像文件
     * @param destFile
     *            压缩后要存放的目标文件
     * @param maxWidth
     *            压缩后允许的最大宽度
     * @param maxHeight
     *            压缩后允许的最大高度
     * @param opacity
     *            水印透明度
     * @param position
     *            水印位置信息
     *
     */
    public static void transform(File sourceFile, File destFile, int maxWidth, int maxHeight, BufferedImage waterMarkImage, float opacity, Position position) throws IOException {
        rotateImage(sourceFile);
        try {
            BufferedImage image = Thumbnails.of(sourceFile).scale(1).rotate(0).asBufferedImage();
            innerTransform(image, destFile, maxWidth, maxHeight, waterMarkImage, opacity, position);
        } catch (Exception e) { // 有时用常规的读取会报错，所以加下面一层
            log.error("transform常规处理出错，使用扩展图片读取器再试一遍。sourceFile:{},destFile:{}", sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
            BufferedImage image = getImage(sourceFile);
            innerTransform(image, destFile, maxWidth, maxHeight, waterMarkImage, opacity, position);
        }
    }

    /**
     * 本来应该根据图片旋转的角度来进行更正
     */
    private static void rotateImage(File sourceFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(sourceFile)) {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(IOUtils.toByteArray(inputStream)));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory == null) {
                return;
            }
            Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            // 进行图片旋转
            if (orientation != null) {
                // 把这些特殊的图片暂时放到试验地
                if (orientation > 1) {
                    FileUtils.copyFile(sourceFile, new File("/tmp/" + sourceFile.getName()));
                }
                int turn = 0;
                int i3 = 3;
                int i6 = 6;
                int i8 = 8;
                if (orientation == i3) {
                    turn = 180;
                } else if (orientation == i6) {
                    turn = 90;
                } else if (orientation == i8) {
                    turn = 270;
                }

                if (turn > 0) {
                    if (directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
                        String mark = directory.getDescription(ExifIFD0Directory.TAG_MAKE);
                        // 测试发现，对于佳能来说，只有旋转360°才回复正常
                        String canon = "Canon";
                        if (canon.equalsIgnoreCase(mark)) {
                            turn = 360;
                        }
                        // 对于iphone，这里不处理，交由下一步
                        String regEx = "[iI][pP]hone";
                        if (Texts.find(mark, regEx)) {
                            return;
                        }
                    }
                    BufferedImage image = rotate(ImageIO.read(sourceFile), turn);
                    ImageIO.write(image, "jpg", sourceFile);
                }
            }
        } catch (ImageProcessingException e1) {
            // nothing
        }
    }

    /**
     * 对角线的水印
     */
    static void innerTransform(BufferedImage sourceImage, File destFile, int maxWidth, int maxHeight, BufferedImage watermarkImage, float opacity, Position position)
            throws IOException {
        if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
            maxHeight = sourceImage.getHeight();
            maxWidth = sourceImage.getWidth();
        }

        Builder<BufferedImage> builder = Thumbnails.of(sourceImage).size(maxWidth, maxHeight);
        BufferedImage image = builder.asBufferedImage();
        // 给网站的图片打上水印
        if (watermarkImage != null) {
            double ratio = (image.getWidth() / 8.0) / watermarkImage.getWidth();
            watermarkImage = Thumbnails.of(watermarkImage).scale(ratio).asBufferedImage();
            builder.watermark(position, watermarkImage, opacity);
        } else {
            log.error("innerTransform 水印图片读取失败。watermarkImage:{}", watermarkImage);
        }
        builder.toFile(destFile);
    }

    /**
     * 随机的水印
     */
    static void innerTransformOfRand(BufferedImage sourceImage, File destFile, int maxWidth, int maxHeight, String waterMarkImagePath) throws IOException {
        if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
            maxHeight = sourceImage.getHeight();
            maxWidth = sourceImage.getWidth();
        }
        Builder<BufferedImage> builder = Thumbnails.of(sourceImage).size(maxWidth, maxHeight).rotate(0);
        // 给网站的图片打上水印
        if (StringUtils.isNotBlank(waterMarkImagePath)) {
            float opacity = 0.4f;
            BufferedImage image = builder.asBufferedImage();
            BufferedImage watermarkImage = ImageIO.read(new File(waterMarkImagePath));
            double ratio = (image.getWidth() / 8.0) / watermarkImage.getWidth();
            watermarkImage = Thumbnails.of(watermarkImage).scale(ratio).asBufferedImage();

            List<Integer> xList = new ArrayList<>();
            List<Integer> yList = new ArrayList<>();
            Position position = (enclosingWidth, enclosingHeight, width, height, insetLeft, insetRight, insetTop, insetBottom) -> {
                int x = getPosition(enclosingWidth, width, xList);
                xList.add(x);
                // noinspection SuspiciousNameCombination
                int y = getPosition(enclosingHeight, height, yList);
                yList.add(y);
                return new Point(x, y);
            };
            builder.watermark(position, watermarkImage, opacity).watermark(position, watermarkImage, opacity).watermark(position, watermarkImage, opacity);
        }
        builder.toFile(destFile);
    }

    /**
     * 根据原图尺寸和水印尺寸获得水印坐标
     */
    private static int getPosition(int enclosingWidth, int width, List<Integer> listPosition) {
        final int x = Randoms.randomInt(width / 2, enclosingWidth - (width + width / 2));
        int width2 = width + width / 2;
        // 如果相距过近，则重新计算
        if (listPosition.stream().anyMatch(t -> x < t + width2 && x > t - width2)) {
            return getPosition(enclosingWidth, width, listPosition);
        }
        return x;
    }

    /**
     * 获得图片真实文件类型
     *
     * @return WebP、JPEG等，如果读取失败，则返回null
     */
    public static String getFileType(InputStream inputStream) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(IOUtils.toByteArray(inputStream)));
            FileTypeDirectory typeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
            return typeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_TYPE_NAME);
        } catch (ImageProcessingException | IOException ignored) {
        }
        return null;
    }

    /**
     * @see #getFileType(InputStream)
     */
    public static String getFileType(File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            return getFileType(fileInputStream);
        } catch (IOException ignored) {
        }
        return null;
    }

    /** 根据文件获得BufferedImage */
    public static BufferedImage getImage(File sourceFile) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(sourceFile); InputStream fileInputStream = new FileInputStream(sourceFile)) {
            // webp图片转换为jpg图片
            if ("webp".equalsIgnoreCase(getFileType(fileInputStream))) {
                WebpIO.toNormalImage(sourceFile, sourceFile);
            }
            // Find potential readers
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            // For each reader: try to read
            while (readers != null && readers.hasNext()) {
                ImageReader reader = readers.next();
                BufferedImage image;
                try {
                    reader.setInput(input);
                    image = reader.read(0);
                    return image;
                } catch (IIOException e) {
                    // Try next reader, ignore.
                } finally {
                    // Close reader resources
                    reader.dispose();
                }
            }

            // Couldn't resize with any of the readers
            throw new IIOException("Unable to resize image:" + sourceFile.getAbsolutePath());
        }
    }

    /**
     * 对图片进行旋转
     *
     * @param src
     *            图片
     * @param angel
     *            角度，一般是90的倍数
     */
    public static BufferedImage rotate(Image src, int angel) {
        int srcWidth = src.getWidth(null);
        int srcHeight = src.getHeight(null);
        // calculate the new image size
        Rectangle rectDes = calcRotatedSize(new Rectangle(new Dimension(srcWidth, srcHeight)), angel);

        BufferedImage res;
        res = new BufferedImage(rectDes.width, rectDes.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = res.createGraphics();
        // transform
        g2.translate((rectDes.width - srcWidth) / 2.0, (rectDes.height - srcHeight) / 2.0);
        g2.rotate(Math.toRadians(angel), srcWidth / 2.0, srcHeight / 2.0);

        g2.drawImage(src, null, null);
        return res;
    }

    private static Rectangle calcRotatedSize(Rectangle src, int angel) {
        int num = 90;
        if (angel >= num) {
            boolean bool = angel / num % 2 == 1;
            if (bool) {
                int temp = src.height;
                // noinspection SuspiciousNameCombination
                src.height = src.width;
                src.width = temp;
            }
            angel = angel % num;
        }

        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2.0;
        double len = 2 * Math.sin(Math.toRadians(angel) / 2.0) * r;
        double angelAlpha = (Math.PI - Math.toRadians(angel)) / 2.0;
        double angelDaltaWidth = Math.atan((double) src.height / src.width);
        double angelDaltaHeight = Math.atan((double) src.width / src.height);

        int lenDaltaWidth = (int) (len * Math.cos(Math.PI - angelAlpha - angelDaltaWidth));
        int lenDaltaHeight = (int) (len * Math.cos(Math.PI - angelAlpha - angelDaltaHeight));
        int desWidth = src.width + lenDaltaWidth * 2;
        int desHeight = src.height + lenDaltaHeight * 2;
        return new Rectangle(new Dimension(desWidth, desHeight));
    }

    /**
     * 往页面输出的方法
     */
    public static void outputImage(BufferedImage image, OutputStream out) throws IOException, NullPointerException {
        // 下面进行对图片格式的一些修改
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
        ImageWriter writer = ImageIO.getImageWriters(type, "jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // 控制图片质量，1.0最高
        param.setCompressionQuality(1.0F);
        // 创建输出流
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(out);
        // 将构建好的图片输出流写入到页面中
        writer.setOutput(outputStream);
        writer.write(null, new IIOImage(image, null, null), param);
    }

    /**
     * 根据文件名后缀判断是否是图片文件
     */
    public static boolean isImage(String fileName) {
        if (!fileName.startsWith(".")) {
            fileName = Files.getExtention(fileName);
        }
        String exten = fileName;
        return Arrays.stream(IMAGE_EXTENTIONS).anyMatch(s -> s.equalsIgnoreCase(exten));
    }

    /**
     * 判断文件是否是图片
     */
    public static boolean isImage(File file) {
        try {
            getImage(file);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 生成图片验证码
     *
     * @param code
     *            需为四位的验证码，如果超出四位，则只取前四位
     */
    public static BufferedImage generateImage(String code) {
        // 设置图片信息，宽，高，具有 8 位 RGB 颜色分量的图像
        BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
        // 得到画笔
        Graphics g = image.getGraphics();
        // 产生背景图片
        g.setColor(Color.white);
        // 画一个矩形框
        g.fillRect(1, 1, 98, 28);
        // 添加一些干扰的线条
        for (int i = 0; i < 20; i++) {
            g.setColor(generateColor());
            int x1 = Randoms.randomInt(0, 100);
            int y1 = Randoms.randomInt(0, 30);
            int x2 = Randoms.randomInt(0, 100);
            int y2 = Randoms.randomInt(0, 30);
            g.drawLine(x1, y1, x2, y2);
        }
        // 画数字
        // 为了得到不同效果的随机字符串，这里采用一个一个字符串的画。
        // 这样可以使其颜色或者其他信息有所不同

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(0) + "", 5, 28);

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(1) + "", 30, 28);

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(2) + "", 55, 28);

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(3) + "", 80, 28);

        // 返回制作好的图像
        return image;
    }

    /**
     * 生成随机的颜色
     */
    static Color generateColor() {
        int r = Randoms.randomInt(0, 180);
        int g = Randoms.randomInt(0, 180);
        int b = Randoms.randomInt(0, 180);
        return new Color(r, g, b);
    }

    /**
     * @see #getColorSpace(InputStream)
     */
    public static String getColorSpace(File imageFile) {
        try (FileInputStream inputStream = new FileInputStream(imageFile)) {
            return getColorSpace(inputStream);
        } catch (IOException e) {
            log.error("getColorSpace", e);
        }
        return null;
    }

    /**
     * 获得图片的Color Space
     *
     * @return 如CMYK等。如果读取出错，则返回null
     */
    public static String getColorSpace(InputStream inputStream) {
        // 获取metadata可能会改变文件，所以这里做一个处理
        try (ByteArrayInputStream stream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(stream);
            IccDirectory iccDirectory = metadata.getFirstDirectoryOfType(IccDirectory.class);
            if (iccDirectory != null) {
                return iccDirectory.getDescription(IccDirectory.TAG_COLOR_SPACE);
            }
        } catch (ImageProcessingException | IOException ignored) {
        }
        return null;
    }

    /**
     * @see #getColorSpace(InputStream)
     */
    public static String getColorTransform(File imageFile) {
        try (FileInputStream inputStream = new FileInputStream(imageFile)) {
            return getColorTransform(inputStream);
        } catch (IOException e) {
            log.error("getColorTransform", e);
        }
        return null;
    }

    /**
     * 获得图片的Color Transform
     *
     * @return 如YCCK等。如果读取出错，则返回null
     */
    public static String getColorTransform(InputStream inputStream) {
        // 获取metadata可能会改变文件，所以这里做一个处理
        try (ByteArrayInputStream stream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(stream);
            AdobeJpegDirectory adobeJpegDirectory = metadata.getFirstDirectoryOfType(AdobeJpegDirectory.class);
            if (adobeJpegDirectory != null) {
                return adobeJpegDirectory.getDescription(AdobeJpegDirectory.TAG_COLOR_TRANSFORM);
            }
        } catch (ImageProcessingException | IOException ignored) {
        }
        return null;
    }

    /** 是否是CMYK颜色空间的图片 */
    public static boolean isCmykColorSpace(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return "CMYK".equalsIgnoreCase(getColorSpace(inputStream));
        } catch (IOException ignored) {
        }
        return false;
    }

    /** 是否是YCCK颜色空间的图片 */
    public static boolean isYcckColorTransform(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return "YCCK".equalsIgnoreCase(getColorTransform(inputStream));
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * 保存html为图片<br>
     * 注意非Windows操作系统可能不支持中文，需要在对应项目的resources下添加typeface/MicrosoftYaHei.ttf文件
     *
     * @param html
     *            内容
     * @param htmlWidth
     *            宽
     * @param htmlHeight
     *            高，-1的话=auto
     * @param outputStream
     *            输出目的地
     */
    public static void saveHtmlToImage(String html, int htmlWidth, int htmlHeight, OutputStream outputStream) {
        File tempFile = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("saveHtmlToImage", "").toFile();
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(html.getBytes())) {
                FileUtils.copyInputStreamToFile(byteArrayInputStream, tempFile);
            }
            Java2DRenderer renderer = new Java2DRenderer(tempFile, htmlWidth, htmlHeight);
            if (!SystemProperties.WINDOWS) {
                AWTFontResolver fontResolver = (AWTFontResolver) renderer.getSharedContext().getFontResolver();
                fontResolver.setFontMapping("Microsoft YaHei", Font.createFont(Font.TRUETYPE_FONT, Resources.getClassPathResourcesInputStream("typeface/MicrosoftYaHei.ttf")));
            }
            FS_IMAGE_WRITER.write(renderer.getImage(), outputStream);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    /**
     * 调整图片透明度
     *
     * @param path
     *            源路径
     * @param tarPath
     *            生成路径
     * @param alpha
     *            透明度 （0不透明---10全透明）
     */
    public static void changeAlpha(String path, String tarPath, int alpha) {
        // 检查透明度是否越界
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 10) {
            alpha = 10;
        }
        try {
            BufferedImage image = ImageIO.read(new File(path));
            int weight = image.getWidth();
            int height = image.getHeight();
            BufferedImage output = new BufferedImage(weight, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = output.createGraphics();
            output = g2.getDeviceConfiguration().createCompatibleImage(weight, height, Transparency.TRANSLUCENT);
            g2.dispose();
            g2 = output.createGraphics();
            // 调制透明度
            for (int j1 = output.getMinY(); j1 < output.getHeight(); j1++) {
                for (int j2 = output.getMinX(); j2 < output.getWidth(); j2++) {
                    int rgb = output.getRGB(j2, j1);
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    output.setRGB(j2, j1, rgb);
                }
            }
            g2.setComposite(AlphaComposite.SrcIn);
            g2.drawImage(image, 0, 0, weight, height, null);
            g2.dispose();
            ImageIO.write(output, "png", new File(tarPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
