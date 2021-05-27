package org.jujubeframework.util;

import com.google.common.io.MoreFiles;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/**
 * 文件工具
 *
 * @author John Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Files {

    /**
     * 获得文件扩展名
     */
    public static String getExtention(String fileName) {
        return getExtention(fileName, "");
    }

    /**
     * 获得文件扩展名（如果扩展名为空，则默认为.${defaultExtension})
     */
    public static String getExtention(String fileName, String defaultExtension) {
        String extension = FilenameUtils.getExtension(fileName);
        return "." + (StringUtils.isEmpty(extension) ? defaultExtension.replace(".", "") : extension);
    }

    /**
     * 创建目录
     */
    public static File createDir(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        File myFile = new File(filePath);
        if (!myFile.exists()) {
            myFile.mkdirs();
        }
        return myFile;
    }

    /**
     * 创建文件。如果上级路径不存在，则创建路径；如果文件不存在，则创建文件
     *
     * @param filePath
     *            文件绝对路径
     */
    public static File createFile(String filePath) {
        Validate.isTrue(StringUtils.isNotBlank(filePath), "文件路径不能为空");
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * 向文件末尾写入内容
     */
    public static File appendStringToFile(String fileName, String data, Charset encoding) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (encoding == null) {
            throw new IllegalArgumentException();
        }
        File file = createFile(fileName);
        if (StringUtils.isBlank(data)) {
            return file;
        }
        try {
            MoreFiles.asCharSink(file.toPath(), encoding, StandardOpenOption.APPEND).write("\r\n" + data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * 根据文件路径判断是否为图片的方法
     */
    public static boolean isImg(String fileUrl) {
        int i = fileUrl.lastIndexOf(".");
        if (i > 0) {
            String ext = fileUrl.substring(i + 1);
            return "gif".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext) || "bmp".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext);
        }
        return false;
    }

    /** base64转成inputStream流 */
    public static InputStream base64ToInputstream(String base64Text) {
        InputStream inputStream = null;
        if (base64Text == null) {
            // 图像数据为空
            return null;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            // 对字符串进行处理
            int j = base64Text.indexOf(',');
            if (j != -1) {
                base64Text = base64Text.substring(j + 1);
            }
            // Base64解码
            byte[] bytes = decoder.decode(base64Text);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
            }
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            log.error("base64ToInputstream", e);
        }
        return inputStream;
    }

    /** base64转存到文件 */
    public static void base64ToFile(String base64Text, File destFile) {
        try {
            IOUtils.copy(base64ToInputstream(base64Text), new FileOutputStream(destFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** inputstream转换为base64 */
    public static String streamToBase64(InputStream inputStream) {
        try {
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 文件转换为base64 */
    public static String fileToBase64(File file) {
        try {
            return streamToBase64(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** 获取规范文件名 */
    public static String canonicalFileName(String name) {
        return name.replaceAll("[`~!@#$%^&*()+=|{}':;,\\[\\].<>?！￥…（）—【】‘；：”“’。，、？]", "").trim();
    }
}
