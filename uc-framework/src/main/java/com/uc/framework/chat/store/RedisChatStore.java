package com.uc.framework.chat.store;

import com.uc.framework.redis.RedisHandler;

/***
 * title: 未 处理的消息存储池 , 带kafka确认的ack消息存储池
 * 
 * @author HadLuo
 *
 */
public class RedisChatStore implements ChatStore {

	@Override
	public void hput(String businessKey, String key, String value) {
		RedisHandler.hset(businessKey, key, value);

	}

	@Override
	public String hget(String businessKey, String key) {
		return RedisHandler.hget(businessKey, key);
	}

	@Override
	public void set(String key, String value) {
		RedisHandler.set(key, value);
	}

	@Override
	public String get(String key) {
		return RedisHandler.get(key);
	}

	@Override
	public void expire(String key, int day) {
		RedisHandler.expireAtDay(key, day);
	}

	@Override
	public void del(String key) {
		RedisHandler.del(key);
	}

}
