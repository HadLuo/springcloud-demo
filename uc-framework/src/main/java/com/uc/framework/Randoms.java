package com.uc.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.util.CollectionUtils;

public class Randoms {

    /**
     * 
     * title: 集合里面随机 一个
     *
     * @param <T>
     * @param datas
     * @return
     * @author HadLuo 2020-9-17 15:47:40
     */
    public static <T> T random(List<T> datas, List<T> exclusions) {
        if (CollectionUtils.isEmpty(datas)) {
            return null;
        }
        if (CollectionUtils.isEmpty(datas)) {
            return datas.get(new Random().nextInt(datas.size()));
        }
        List<T> temp = new ArrayList<>();
        for (T data : datas) {
            if (!exclusions.contains(data)) {
                // 不在排除的集合里面
                temp.add(data);
            }
        }
        if (CollectionUtils.isEmpty(temp)) {
            return null;
        }
        return temp.get(new Random().nextInt(temp.size()));
    }

}
