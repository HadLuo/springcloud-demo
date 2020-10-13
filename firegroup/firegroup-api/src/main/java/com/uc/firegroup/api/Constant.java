package com.uc.firegroup.api;

public final class Constant {

    /***
     * 项目实例名称
     */
    public static final String instanceName = "sq-firegroup";

    /** redis key 过期时间 */
    public static final int RedisExpireTime = 60 * 60 * 24 * 30;

    /**
     * 新增的水军计数
     */
    public static final String RedisAddRobotNumKey = "RedisAddRobotNumKey";
}
