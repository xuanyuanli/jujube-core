package org.jujubeframework.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Position;
import org.apache.commons.io.FileUtils;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Images {

    /**
     * 水印图片地址
     */
    public static final String PROJECT_WATERMARK_PATH = "watermark.png";

    /**
     * 等比压缩图像(默认带水印)
     *
     * @param sourceFile 源图像文件
     * @param destFile   压缩后要存放的目标文件
     * @param maxWidth   压缩后允许的最大宽度
     * @param maxHeight  压缩后允许的最大高度
     * @throws IOException
     */
    public static void transform(File sourceFile, File destFile, int maxWidth, int maxHeight) throws IOException {
        boolean addWatermark = true;
        // _c表示已经加过水印了
        String suffix = "_c.";
        if (sourceFile != null && sourceFile.getName().contains(suffix)) {
            addWatermark = false;
        }
        transform(sourceFile, destFile, maxWidth, maxHeight, addWatermark);
    }

    /**
     * @see transform(File sourceFile, File destFile, int maxWidth, int
     * maxHeight) throws IOException
     */
    public static void transform(File sourceFile, File destFile, int maxWidth, int maxHeight, boolean addWatermark) throws IOException {
        rotateImage(sourceFile);

        try {
            BufferedImage image = Thumbnails.of(sourceFile).scale(1).rotate(0).asBufferedImage();
            innerTransform(image, destFile, maxWidth, maxHeight, addWatermark);
        } catch (IIOException e) { // 有时用常规的读取会报错，所以加下面一层
            BufferedImage image = getImage(sourceFile);
            innerTransform(image, destFile, maxWidth, maxHeight, addWatermark);
        }
    }

    /**
     * 本来应该根据图片旋转的角度来进行更正
     */
    private static void rotateImage(File sourceFile) throws IOException {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(sourceFile);
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
    static void innerTransform(BufferedImage sourceImage, File destFile, int maxWidth, int maxHeight, boolean haveWatermark) throws IOException {
        if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
            maxHeight = sourceImage.getHeight();
            maxWidth = sourceImage.getWidth();
        }

        Builder<BufferedImage> builder = Thumbnails.of(sourceImage).size(maxWidth, maxHeight);
        // 给网站的图片打上水印
        if (haveWatermark) {
            float opacity = 0.35f;
            BufferedImage image = builder.asBufferedImage();
            BufferedImage watermarkImage = ImageIO.read(new File(Images.PROJECT_WATERMARK_PATH));
            double ratio = (image.getWidth() / 8.0) / watermarkImage.getWidth();
            watermarkImage = Thumbnails.of(watermarkImage).scale(ratio).asBufferedImage();

            Position[] positions = PositivePositions.values();
            builder.watermark(positions[1], watermarkImage, opacity);
        }
        builder.toFile(destFile);
    }

    /**
     * 随机的水印
     */
    static void innerTransformOfRand(BufferedImage sourceImage, File destFile, int maxWidth, int maxHeight, boolean haveWatermark) throws IOException {
        if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
            maxHeight = sourceImage.getHeight();
            maxWidth = sourceImage.getWidth();
        }
        Builder<BufferedImage> builder = Thumbnails.of(sourceImage).size(maxWidth, maxHeight).rotate(0);
        // 给网站的图片打上水印
        if (haveWatermark) {
            float opacity = 0.4f;
            BufferedImage image = builder.asBufferedImage();
            BufferedImage watermarkImage = ImageIO.read(new File(Images.PROJECT_WATERMARK_PATH));
            double ratio = (image.getWidth() / 8.0) / watermarkImage.getWidth();
            watermarkImage = Thumbnails.of(watermarkImage).scale(ratio).asBufferedImage();

            List<Integer> listX = new ArrayList<>();
            List<Integer> listY = new ArrayList<>();
            Position position = new Position() {
                @Override
                public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                    int x = getPosition(enclosingWidth, width, listX);
                    listX.add(x);
                    int y = getPosition(enclosingHeight, height, listY);
                    listY.add(y);
                    return new Point(x, y);
                }
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

    public static BufferedImage getImage(File sourceFile) throws IOException {
        ImageInputStream input = null;
        try {
            input = ImageIO.createImageInputStream(sourceFile);
            // Find potential readers
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            // For each reader: try to read
            while (readers != null && readers.hasNext()) {
                ImageReader reader = readers.next();
                BufferedImage image = null;
                try {
                    reader.setInput(input);
                    image = reader.read(0);
                    return image;
                } catch (IIOException e) {
                    // Try next reader, ignore.
                } catch (Exception e) {
                    // Unexpected exception. do not continue
                    throw e;
                } finally {
                    // Close reader resources
                    reader.dispose();
                }
            }

            // Couldn't resize with any of the readers
            throw new IIOException("Unable to resize image:" + sourceFile.getAbsolutePath());
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * 对图片进行旋转
     */
    public static BufferedImage rotate(Image src, int angel) {
        int srcWidth = src.getWidth(null);
        int srcHeight = src.getHeight(null);
        // calculate the new image size
        Rectangle rectDes = calcRotatedSize(new Rectangle(new Dimension(srcWidth, srcHeight)), angel);

        BufferedImage res = null;
        res = new BufferedImage(rectDes.width, rectDes.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = res.createGraphics();
        // transform
        g2.translate((rectDes.width - srcWidth) / 2.0, (rectDes.height - srcHeight) / 2.0);
        g2.rotate(Math.toRadians(angel), srcWidth / 2.0, srcHeight / 2.0);

        g2.drawImage(src, null, null);
        return res;
    }

    public static Rectangle calcRotatedSize(Rectangle src, int angel) {
        int num = 90;
        if (angel >= num) {
            boolean bool = angel / num % 2 == 1;
            if (bool) {
                int temp = src.height;
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

    public static int getOrientation(String orientation) {
        orientation = Dynamics.orElse(orientation, "");
        int tag = 0;
        switch (orientation) {
            case "Top, left side (Horizontal / normal)":
                tag = 1;
                break;
            case "Top, right side (Mirror horizontal)":
                tag = 2;
                break;
            case "Bottom, right side (Rotate 180)":
                tag = 3;
                break;
            case "Bottom, left side (Mirror vertical)":
                tag = 4;
                break;
            case "Left side, top (Mirror horizontal and rotate 270 CW)":
                tag = 5;
                break;
            case "Right side, top (Rotate 90 CW)":
                tag = 6;
                break;
            case "Right side, bottom (Mirror horizontal and rotate 90 CW)":
                tag = 7;
                break;
            case "Left side, bottom (Rotate 270 CW)":
                tag = 8;
                break;
            default:
                tag = 0;
                break;
        }
        return tag;
    }

    /**
     * 往页面输出的方法
     */
    public static void outputImage(BufferedImage image, ServletOutputStream out) throws IOException, NullPointerException {
        ImageWriter writer = null;
        // 下面进行对图片格式的一些修改
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
        Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "jpg");
        if (iter.hasNext()) {
            writer = iter.next();
        }

        IIOImage iioImage = new IIOImage(image, null, null);
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // 控制图片质量，1.0最高
        param.setCompressionQuality(1.0F);
        // 创建输出流
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(out);
        // 将构建好的图片输出流写入到页面中
        writer.setOutput(outputStream);
        writer.write(null, iioImage, param);
    }

    /**
     * 正对角线
     */
    private enum PositivePositions implements Position {
        /**
         * TOP_LEFT
         */
        TOP_LEFT() {
            @Override
            public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                int cellWidth = enclosingWidth / 3;
                int cellHeight = enclosingHeight / 3;
                int x = cellWidth / 2 - (width / 2);
                int y = cellHeight / 4 - (height / 2);
                return new Point(x, y);
            }
        },
        /**
         * CENTER
         */
        CENTER() {
            @Override
            public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                int x = (enclosingWidth / 2) - (width / 2);
                int y = (enclosingHeight / 2) - (height / 2);
                return new Point(x, y);
            }
        },
        /**
         * BOTTOM_RIGHT
         */
        BOTTOM_RIGHT() {
            @Override
            public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                int cellWidth = enclosingWidth / 3;
                int cellHeight = enclosingHeight / 3;
                int x = cellWidth * 2 + cellWidth / 2 - (width / 2);
                int y = cellHeight * 2 + (int) (cellHeight * 0.75) - (height / 2);
                return new Point(x, y);
            }
        }
    }

    /**
     * 反对角线
     */
    @SuppressWarnings("unused")
    private enum NegativePositions implements Position {
        /**
         * TOP_RIGHT
         */
        TOP_RIGHT() {
            @Override
            public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                int cellWidth = enclosingWidth / 3;
                int cellHeight = enclosingHeight / 3;
                int x = cellWidth * 2 + cellWidth / 2 - (width / 2);
                int y = cellHeight / 4 - (height / 2);
                return new Point(x, y);
            }
        },
        /**
         * CENTER
         */
        CENTER() {
            @Override
            public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                int x = (enclosingWidth / 2) - (width / 2);
                int y = (enclosingHeight / 2) - (height / 2);
                return new Point(x, y);
            }
        },
        /**
         * BOTTOM_LEFT
         */
        BOTTOM_LEFT() {
            @Override
            public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft, int insetRight, int insetTop, int insetBottom) {
                int cellWidth = enclosingWidth / 3;
                int cellHeight = enclosingHeight / 3;
                int x = cellWidth / 2 - (width / 2);
                int y = cellHeight * 2 + (int) (cellHeight * 0.75) - (height / 2);
                return new Point(x, y);
            }
        }
    }
}
