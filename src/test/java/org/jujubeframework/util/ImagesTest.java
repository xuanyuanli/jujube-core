package org.jujubeframework.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.util.StopWatch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImagesTest {

    @Test
    public void isImage() {
        String fileName = "a.jpeg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.jpg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.png";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.bmp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.webp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();

        fileName = ".Jpeg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".jPg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".PNG";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".bmp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".webp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
    }

    @Test
    public void isImage2() throws IOException {
        Resource resource = Resources.getClassPathResources("material/true.png");
        Assertions.assertThat(Images.isImage(resource.getFile())).isTrue();

        resource = Resources.getClassPathResources("material/false.jpg");
        Assertions.assertThat(Images.isImage(resource.getFile())).isFalse();

        resource = Resources.getClassPathResources("material/false1.jpg");
        Assertions.assertThat(Images.isImage(resource.getFile())).isFalse();
    }

    public static void main(String[] args) throws IOException, ImageProcessingException {
        test4();
    }


    private static void test4() throws IOException {
        String path = "D:\\data\\picture\\cmyk\\not right.jpg";
        System.out.println(Images.getColorSpace(new File(path)));
        System.out.println(Images.getColorTransform(new File(path)));
        String destPath = "D:\\data\\picture\\cmyk\\not right-new.jpg";
        BufferedImage srcImage = Images.getImage(new File(path));
        // Thumbnails.of(srcImage).size(srcImage.getWidth(),
        // srcImage.getHeight()).toFile(destPath);
        Images.outputImage(srcImage, new FileOutputStream(destPath));
        System.out.println(Images.getColorSpace(new File(path)));
        System.out.println(Images.getColorTransform(new File(path)));
    }

    private static void test3() throws ImageProcessingException, IOException {
        String path = "D:\\data\\picture\\cmyk\\not right.jpg";
        // String path = "D:\\data\\picture\\cat.jpg";
        Metadata metadata = ImageMetadataReader.readMetadata(new FileInputStream(path));
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.format("[%s] - %s = %s\n", directory.getName(), tag.getTagName(), tag.getDescription());
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.err.format("ERROR: %s\n", error);
                }
            }
        }
    }

    private static void test2() {
        String path = "D:\\data\\picture\\cmyk\\not right.jpg";
        System.out.println(Images.getColorSpace(new File(path)));

        path = "D:\\data\\picture\\cmyk\\right.jpg";
        System.out.println(Images.getColorSpace(new File(path)));
    }

    private static void test1() throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String path = "D:\\data\\picture\\cmyk\\not right.jpg";
        System.out.println(Images.getColorSpace(new File(path)));
        stopWatch.stop();
        System.out.println("判断：" + stopWatch.getLastTaskTimeMillis());

        String destPath = "D:\\data\\picture\\CMYK-2-rpg.jpg";
        stopWatch.start();
        Images.transformOrigin(new File(path), new File(destPath), 5000, 5000);
        stopWatch.stop();
        System.out.println("转换：" + stopWatch.getLastTaskTimeMillis());
    }

}