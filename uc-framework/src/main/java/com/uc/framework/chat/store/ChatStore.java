package com.uc.framework.chat.store;

/***
 * title :数据存储层
 * 
 * @author HadLuo
 *
 */
public interface ChatStore extends HashStore, StringStore {

	public void expire(String key, int afterDay);

	public void del(String key);
}
