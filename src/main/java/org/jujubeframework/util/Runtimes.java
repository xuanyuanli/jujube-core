package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jujubeframework.constant.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 运行时工具类
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Runtimes {

    public static final char AT_SYMBOL = '@';
    private static Logger logger = LoggerFactory.getLogger(Runtimes.class);
    private static String ZK_PATH = "";
    private static String ZK_BAT_NAME = "startZookeeperServer.bat";

    private static String getZkPath() {
        if (SystemProperties.WINDOWS && ZK_PATH.length() == 0) {
            File projectPath = new File(Utils.getProjectPath());
            while (true) {
                String name = projectPath.getName();
                if ("service".equals(name) || "base".equals(name) || "schedule".equals(name) || "application".equals(name) || name.endsWith("-application")) {
                    break;
                }
                projectPath = projectPath.getParentFile();
            }
            ZK_PATH = projectPath.getParentFile().getAbsolutePath() + "/alone-server/zookeeper/".replace("/", File.separator);
        }
        return ZK_PATH;
    }

    /**
     * 执行命令并获得输出
     */
    public static Process execCommand(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            return process;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行命令并获得输出,编码默认为gbk
     */
    public static String execCommandAndGetInput(String command) {
        return execCommandAndGetInput(command, "gbk");
    }

    /**
     * 执行命令并获得输出
     */
    public static String execCommandAndGetInput(String command, String charset) {
        String result = "";
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);
            result = IOUtils.toString(process.getInputStream(), charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 运行bat文件(仅限windows环境下使用)
     */
    public static void runBat(String batPath, String batName) {
        Validate.isTrue(SystemProperties.WINDOWS);
        Process ps = null;
        try {
            // 盘符
            String drive = batPath.split(":")[0] + ":";
            String command = "cmd /c " + drive + " && cd " + batPath;
            command += " && cmd /c start " + batName;
            ps = Runtime.getRuntime().exec(command);
            ps.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 接收执行完毕的返回值
        int i = ps.exitValue();
        if (i == 0) {
            logger.info("命令：{} 执行完成.", batName);
        } else {
            logger.info("命令：{} 执行失败.", batName);
        }
    }

    /**
     * 运行zk启动脚步(仅限windows环境下使用)
     */
    public static void runZookeeperStartBat() {
        runBat(getZkPath(), ZK_BAT_NAME);
    }

    /**
     * 获得java进程id
     *
     * @return java进程id
     */
    public static final int getPid() {
        String pid = System.getProperty("pid");
        if (pid == null) {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String processName = runtimeMXBean.getName();
            if (processName.indexOf(AT_SYMBOL) != -1) {
                pid = processName.substring(0, processName.indexOf('@'));
            }
        }
        return Texts.toInt(pid);
    }

    /**
     * 获得运行时的程序名称
     */
    public static String getRuntimeJarName() {
        String input = execCommandAndGetInput("jps -l", "utf-8");
        if (StringUtils.isNotBlank(input)) {
            String[] arr = input.split("\n|\r");
            String vmid = getPid() + " ";
            for (String ele : arr) {
                if (ele.startsWith(vmid)) {
                    return ele.split("\\s+")[1];
                }
            }
        }
        return null;
    }

    /**
     * 线程睡眠
     */
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("线程睡眠", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获得主机名
     */
    public static String getHostName() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            // host = "hostname: hostname"
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }
}
