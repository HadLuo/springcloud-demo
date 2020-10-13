package com.uc.framework.chat;

import java.util.List;

public class ChatGroupFactory {

	public static ChatGroup create(String groupUuid, List<Chat> chats) {
		ChatGroup chatGroup = new ChatGroup();
		chatGroup.setChats(chats);
		chatGroup.setGroupUuid(groupUuid);
		return chatGroup;
	}

}
