package org.jujubeframework.util.net;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.*;

/**
 * ftp上传与下载工具
 * <p>
 * 使用完之后，记得disconnect
 *
 * @author John Li
 */
@Slf4j
public class Ftps {
    private final String ip;
    private final int port;
    private final String username;
    private final String password;
    private final String account;

    private String systemKey = FTPClientConfig.SYST_UNIX;
    private String serverLanguageCode = FTP.DEFAULT_CONTROL_ENCODING;

    private int bufSize = 1024;
    private int fileType = FTP.BINARY_FILE_TYPE;

    private String rootWorkingDirectory = "/";

    private FTPClient ftpClient;

    public Ftps(String ip, int port, String username, String password, String account) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.account = account;
        init();
    }

    public Ftps(String ip, String username, String password, String account) {
        this(ip, 0, username, password, account);
    }

    public Ftps(String ip, String username, String password) {
        this(ip, 0, username, password, null);
    }

    /**
     * 初始化ftp客户端
     */
    public void init() {
        ftpClient = new FTPClient();
        ftpClient.configure(getFtpConfig());

        boolean isLogin = false;
        try {

            if (port > 0) {
                ftpClient.connect(ip, port);
            } else {
                ftpClient.connect(ip);
            }

            if (StringUtils.isNotBlank(account)) {
                isLogin = ftpClient.login(username, password, account);
            } else {
                isLogin = ftpClient.login(username, password);
            }
        } catch (Exception e) {
            log.error("初始化ftp服务器", e);
        }
        if (!isLogin) {
            throw new RuntimeException("login failed");
        }
        ftpClient.setBufferSize(bufSize);
        // 设置文件类型（二进制）
        try {
            ftpClient.setFileType(fileType);
        } catch (IOException e) {
            log.error("ftpClient.setFileType", e);
        }
        log.info("连接到ftp服务器{}:{},登录成功...", ip, port);
    }

    private FTPClientConfig getFtpConfig() {
        FTPClientConfig ftpConfig = new FTPClientConfig(systemKey);
        ftpConfig.setServerLanguageCode(serverLanguageCode);
        return ftpConfig;
    }

    /**
     * 下载文件
     *
     * @param ftpFilePath
     *            ftp文件地址
     * @param localFilePath
     *            下载到本地的地址
     */
    public boolean download(String ftpFilePath, String localFilePath) {
        boolean result;
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(localFilePath))) {
            result = ftpClient.retrieveFile(ftpFilePath, fos);
            fos.flush();
        } catch (IOException e) {
            log.error("download file", e);
            result = false;
        }
        return result;
    }

    /**
     * 重命名文件
     *
     * @param oldFileName
     *            原文件名
     * @param newFileName
     *            新文件名
     */
    public boolean renameFile(String oldFileName, String newFileName) {
        try {
            return ftpClient.rename(oldFileName, newFileName);
        } catch (IOException ioe) {
            log.error("rename file", ioe);
        }
        return false;
    }

    /**
     * 在服务器上创建一个文件夹
     *
     * @param dir
     *            文件夹名称，不能含有特殊字符，如 \ 、/ 、: 、* 、?、 "、 &lt;、&gt;...
     */
    public boolean makeDirectory(String dir) {
        try {
            return ftpClient.makeDirectory(dir);
        } catch (Exception e) {
            log.error("make Directory", e);
        }
        return false;
    }

    /**
     * 删除一个文件
     */
    public boolean deleteFile(String filename) {
        try {
            return ftpClient.deleteFile(filename);
        } catch (IOException ioe) {
            log.error("deleteFile", ioe);
        }
        return false;
    }

    /**
     * 删除目录
     */
    public boolean deleteDirectory(String pathname) {
        boolean flag;
        try {
            File file = new File(pathname);
            if (file.isDirectory()) {
                flag = ftpClient.removeDirectory(pathname);
            } else {
                flag = deleteFile(pathname);
            }
        } catch (IOException ioe) {
            log.error("deleteDirectory", ioe);
            flag = false;
        }
        return flag;
    }

    /**
     * 上传文件到服务器
     *
     * @param localFile
     *            本地文件
     * @param destPath
     *            服务器绝对路径（包含文件名）
     */
    public boolean uploadFile(File localFile, final String destPath) {
        boolean flag = true;
        try (InputStream localFileInput = new BufferedInputStream(new FileInputStream(localFile))) {
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

            File destFile = new File(destPath);
            ftpClient.changeWorkingDirectory(rootWorkingDirectory);
            ftpClient.makeDirectory(destFile.getParent());
            ftpClient.changeWorkingDirectory(destFile.getParent());

            flag = ftpClient.storeFile(destFile.getName(), localFileInput);
        } catch (Exception e) {
            log.error("uploadFile", e);
        }
        return flag;
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            log.error("关闭ftp服务器", e);
        }
    }

    /**
     * 设置ftp服务端的操作系统
     *
     * @param systemKey
     *            参考：FTPClientConfig.SYST_*
     */
    public void setSystemKey(String systemKey) {
        this.systemKey = systemKey;
    }

    /**
     * 设置服务器语言编码
     *
     * @param serverLanguageCode
     *            默认为FTP.DEFAULT_CONTROL_ENCODING
     */
    public void setServerLanguageCode(String serverLanguageCode) {
        this.serverLanguageCode = serverLanguageCode;
    }

    /**
     * 设置bufferSize
     *
     * @param bufSize
     *            默认为1024
     */
    public void setBufSize(int bufSize) {
        this.bufSize = bufSize;
    }

    /**
     * 设置fileType，可选值：FTP.*_FILE_TYPE
     *
     * @param fileType
     *            默认为二进制传输
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    /**
     * 设置root工作目录
     *
     * @param rootWorkingDirectory
     *            默认ftp服务器端操作系统为linux，所以root工作目录为"/"
     */
    public void setRootWorkingDirectory(String rootWorkingDirectory) {
        this.rootWorkingDirectory = rootWorkingDirectory;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }
}
