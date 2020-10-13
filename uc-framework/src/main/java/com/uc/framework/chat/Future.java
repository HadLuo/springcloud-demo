package com.uc.framework.chat;

public interface Future {

    /***
     * 
     * title: 获取 发送成功的跟踪key
     *
     * @return
     * @author HadLuo 2020-9-27 15:45:43
     */
    public String getAckKey();

    /***
     * 
     * title: 获取 发送成功的群id
     *
     * @return
     * @author HadLuo 2020-9-27 15:45:43
     */
    public String getGroupId();

    public String getErrorMessage();

    public String getUuid();

    /***
     * 
     * title: 构造future
     *
     * @param <V>
     * @param isHalf
     * @param arg
     * @return
     * @author HadLuo 2020-9-27 15:45:01
     */
    public static Future newErrorFuture(String groupId, String ackKey, String errorMessage) {
        return new ErrorFuture(groupId, ackKey, errorMessage);
    }

    /***
     * 
     * title: 构造future
     *
     * @param <V>
     * @param isHalf
     * @param arg
     * @return
     * @author HadLuo 2020-9-27 15:45:01
     */
    public static Future newSuccessFuture(String key, String groupId) {
        return new SuccessFuture(key, groupId);
    }

    /***
     * 
     * title: 构造future
     *
     * @param <V>
     * @param isHalf
     * @param arg
     * @return
     * @author HadLuo 2020-9-27 15:45:01
     */
    public static Future newPauseFuture(String uuid, String groupId) {
        return new PauseFuture(uuid, groupId);
    }
}
