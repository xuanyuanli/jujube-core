package org.jujubeframework.util.net;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.*;
import java.util.Enumeration;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Networks {

    /**
     * 获取本地MAC地址的方法
     */
    public static String getLocalMacAddress() {
        try {
            // 获取本地IP对象
            InetAddress ia = InetAddress.getLocalHost();
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

            // 下面代码是把mac地址拼装成String
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            return "TT-TT-TT-TT";
        }
    }

    /**
     * 获得主机名
     */
    public static String getHostName() {
        InetAddress netAddress;
        try {
            netAddress = InetAddress.getLocalHost();
            return netAddress.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static void bindPort(String host, int port) throws Exception {
        try (Socket s = new Socket()) {
            s.bind(new InetSocketAddress(host, port));
        }
    }

    /**
     * 端口是否可用
     */
    public static boolean isPortAvailable(int port) {
        try {
            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断ip是否是本地IP
     */
    public static boolean isLocalIp(String qip) {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (!netInterface.isUp() || netInterface.isLoopback() || netInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address && ip.getHostAddress().equals(qip)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
