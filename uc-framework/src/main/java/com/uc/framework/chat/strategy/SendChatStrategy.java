package com.uc.framework.chat.strategy;

import com.uc.framework.chat.Chat;
import com.uc.framework.chat.Future;

/***
 * title : 发送消息策略
 * @author HadLuo
 *
 */
public interface SendChatStrategy {
	/***
	 * 
	 * title: 异步发送 消息
	 *
	 * @param chat 当前要发送的聊天消息包
	 * @return Future 返回结果
	 * @author HadLuo 2020-9-27 15:20:59
	 */
	public Future asyncSend(Chat chat);

}
