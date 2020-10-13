package com.uc.framework.chat.store;

public interface HashStore {

	/***
	 * title: 设置值
	 * 
	 * @param businessKey
	 * @param groupWxId
	 * @param sort
	 */
	public void hput(String businessKey, String key, String value);

	/***
	 * title : 获取值
	 * 
	 * @param businessKey
	 * @param key
	 * @return
	 */
	public String hget(String businessKey, String key);

}
