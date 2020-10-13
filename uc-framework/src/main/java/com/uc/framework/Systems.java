package com.uc.framework;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.springframework.util.StringUtils;
import com.uc.framework.logger.Logs;

/***
 * 操作系统 工具
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2019年3月15日 新建
 */
public class Systems {

    /***
     * 获取工程名
     * 
     * @return
     * @author HadLuo 2018年12月3日 新建
     */
    public static String getProjectName() {
        String projectname = System.getProperty("user.dir");
        if (StringUtils.isEmpty(projectname)) {
            return "未知工程";
        }
        if (projectname.contains("tomcat")) {
            // 是 tomcat 接入层
            // file:/usr/local/yunji/tomcat/lyadmin/webapps/lyadmin/WEB-INF/classes/
            projectname = Thread.currentThread().getContextClassLoader().getResource("").toString();
            try {
                // 截取 lyadmin 接入层 名称
                projectname = projectname.substring(projectname.indexOf("tomcat") + "tomcat/".length(),
                        projectname.indexOf("webapps") - 1);
            } catch (Exception e) {
                Logs.e(System.class, "获取工程名失败>>projectname=" + projectname);
                e.printStackTrace();
            }
            return "【接入层】" + projectname;
        }
        try {
            return "【服务】" + projectname.substring(projectname.lastIndexOf(File.separator) + 1,
                    projectname.length());
        } catch (Exception e) {
            e.printStackTrace();
            return "未知工程";
        }
    }

    /***
     * 获取 本机ip
     * 
     * @return
     * @author HadLuo 2018年12月3日 新建
     */
    public static String getLocalIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            try {
                return getInetAddress().getHostAddress();
            } catch (Exception e2) {
            }
        }
        return "";
    }

    private static boolean isWindowOS() {
        boolean isWindowOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowOS = true;
        }
        return isWindowOS;
    }

    private static InetAddress getInetAddress() {
        InetAddress inetAddress = null;
        try {
            // 如果是windows操作系统
            if (isWindowOS()) {
                inetAddress = InetAddress.getLocalHost();
            } else {
                boolean bFindIP = false;
                // 定义一个内容都是NetworkInterface的枚举对象
                Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
                        .getNetworkInterfaces();
                // 如果枚举对象里面还有内容(NetworkInterface)
                while (netInterfaces.hasMoreElements()) {
                    if (bFindIP) {
                        break;
                    }
                    // 获取下一个内容(NetworkInterface)
                    NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                    // ----------特定情况，可以考虑用ni.getName判断
                    // 遍历所有IP
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        inetAddress = (InetAddress) ips.nextElement();
                        if (inetAddress.isSiteLocalAddress() // 属于本地地址
                                && !inetAddress.isLoopbackAddress() // 不是回环地址
                                && inetAddress.getHostAddress().indexOf(":") == -1) { // 地址里面没有:号
                            bFindIP = true; // 找到了地址
                            break; // 退出循环
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inetAddress;
    }
}
