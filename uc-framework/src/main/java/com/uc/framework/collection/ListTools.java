package com.uc.framework.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ListTools {

	public static interface SelectOne<T, R> {
		public R select(T item);
	}

	/***
	 * title ： 将 集合 按照特定的字段值分组成map
	 * @param <R>
	 * @param <T>
	 * @param sources
	 * @param selectOne
	 * @return
	 */
	public static <R, T> Map<R, List<T>> group(List<T> sources, SelectOne<T, R> selectOne) {
		if (CollectionUtils.isEmpty(sources)) {
			return new HashMap<R, List<T>>(0);
		}
		Map<R, List<T>> map = Maps.newHashMap();
		for (T item : sources) {
			R key = selectOne.select(item);
			List<T> val = map.get(key);
			if (val == null) {
				val = Lists.newArrayList();
				map.put(key, val);
			}
			val.add(item);
		}
		return map;
	}

	/**
	 * 抽取集合对象中 某字段 成为另外一个集合
	 * 
	 * @param <T>
	 * @param <R>
	 * @param sources
	 * @param selectOne
	 * @return
	 */
	public static <T, R> List<R> extract(List<T> sources, SelectOne<T, R> selectOne) {
		if (CollectionUtils.isEmpty(sources)) {
			return Collections.emptyList();
		}
		List<R> list = Lists.newArrayList();
		for (T item : sources) {
			list.add(selectOne.select(item));
		}
		return list;
	}

	/**
	 * 
	 * title: 从集合中找出 符合特定规则 的 某一项
	 *
	 * @param sources
	 * @param itemExpectVal 集合中预期的值
	 * @param selectOne
	 * @return
	 * @author HadLuo 2020-9-18 15:41:02
	 * @param <R>
	 */
	public static <T, R> T selectOne(List<T> sources, Object itemExpectVal, SelectOne<T, R> selectOne) {
		if (CollectionUtils.isEmpty(sources)) {
			return null;
		}
		if (selectOne == null) {
			return null;
		}
		for (T item : sources) {
			Object v = selectOne.select(item);
			if (v == null && itemExpectVal == null) {
				return item;
			}
			if (v != null && v.equals(itemExpectVal)) {
				return item;
			}
		}
		return null;
	}

}
