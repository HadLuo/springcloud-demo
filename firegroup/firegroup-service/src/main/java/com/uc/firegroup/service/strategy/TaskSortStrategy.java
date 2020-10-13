package com.uc.firegroup.service.strategy;

import java.util.Comparator;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.framework.Objects;

public class TaskSortStrategy implements Comparator<PushTask> {

    @Override
    public int compare(PushTask o1, PushTask o2) {
        Integer sort1 = 0, sort2 = 0;
        if (o1.getPlayMessage() != null) {
            sort1 = o1.getPlayMessage().getMessageSort();
        }
        if (o2.getPlayMessage() != null) {
            sort2 = o2.getPlayMessage().getMessageSort();
        }
        return Objects.wrapNull(sort1) - Objects.wrapNull(sort2, 0);
    }

}
