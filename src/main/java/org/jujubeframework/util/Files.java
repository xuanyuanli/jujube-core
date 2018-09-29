package org.jujubeframework.util;

import com.google.common.io.FileWriteMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Files {
    /**
     * 获得文件扩展名
     */
    public static String getExtention(String fileName) {
        return "." + FilenameUtils.getExtension(fileName);
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
     * @param filePath 文件绝对路径
     * @return
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
            com.google.common.io.Files.asCharSink(file, encoding, FileWriteMode.APPEND).write("\r\n" + data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

}
