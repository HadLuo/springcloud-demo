package com.uc.framework.chat.strategy;

import java.util.Comparator;
import com.uc.framework.Objects;
import com.uc.framework.chat.Chat;

/***
 * 
 * title: 消息 排序策略
 *
 * @author HadLuo
 * @date 2020-9-27 11:14:30
 */
public class ChatSortStrategy implements Comparator<Chat> {
    @Override
    public int compare(Chat o1, Chat o2) {
        Integer sort1 = o1.getSort(), sort2 = o2.getSort();
        return Objects.wrapNull(sort1) - Objects.wrapNull(sort2, 0);
    }

}
