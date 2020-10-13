package com.uc.framework.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import com.alibaba.fastjson.util.IOUtils;
import com.google.common.collect.Lists;

/**
 * 
 * 文件加载器 （搜索的路径为 项目执行的任何 环境变量下）
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2019年3月21日 新建
 */
public class FileLoads {

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.";

    /**
     * 将环境变量下的文件解析成Properties
     * 
     * @param fileName 文件名称（包括后缀， 如：11.txt）
     * @author HadLuo
     * @since JDK1.7
     * @history 2019年3月21日 新建
     */
    public static Properties loadProperties(String fileName) {
        InputStream input = loadInputStream(fileName);
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(input, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        IOUtils.close(input);
        return properties;
    }

    /***
     * 
     * 遍历 环境变量下 文件下的所有文件（注意：不是递归，只是找一层）
     * 
     */
    public static List<File> listFolder(String folder) {
        List<File> files = Lists.newArrayList();
        String DEFAULT_RESOURCE_PATTERN = "**/" + folder + "/*";
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            // 从第三方 jar 包中加载
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + org.springframework.util.ClassUtils.convertClassNameToResourcePath(
                            SystemPropertyUtils.resolvePlaceholders(""))
                    + "/" + DEFAULT_RESOURCE_PATTERN;
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                files.add(resource.getFile());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return files;
    }

    public static InputStream loadInputStream(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String DEFAULT_RESOURCE_PATTERN = "**/*.";
        String endPrifix = fileName.substring(fileName.lastIndexOf(".") + 1);
        DEFAULT_RESOURCE_PATTERN = DEFAULT_RESOURCE_PATTERN + endPrifix;
        // 这里 可以 读取属性配置文件 ，我这里就不写了
        try {
            InputStream in = FileLoads.class.getClassLoader().getResourceAsStream(fileName);
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            if (in == null) {
                // 从第三方 jar 包中加载
                String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                        + org.springframework.util.ClassUtils.convertClassNameToResourcePath(
                                SystemPropertyUtils.resolvePlaceholders(""))
                        + "/" + DEFAULT_RESOURCE_PATTERN;
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    if (resource.getFilename().trim().equals(fileName.trim())) {
                        in = resource.getInputStream();
                        break;
                    }
                }
            }
            if (in == null) {
                throw new RuntimeException("文件找不到异常【" + fileName + " 文件不存在(当前项目和第三方项目执行环境下都不存在)】");
            }
            return in;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("文件找不到异常【" + fileName + " 文件不存在(当前项目和第三方项目执行环境下都不存在)】", e);
        }
    }

    /**
     * 将环境变量下的文件解析成string
     * 
     * @param fileName 文件名称（包括后缀， 如：11.txt）
     * @author HadLuo
     * @since JDK1.7
     * @history 2019年3月21日 新建
     */
    public static String loadString(String fileName) {
        InputStream input = loadInputStream(fileName);
        byte[] bytes = null;
        try {
            int available = input.available();
            if (available > 0) {
                bytes = new byte[available];
                input.read(bytes);
                return new String(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        IOUtils.close(input);
        return "";
    }
}
