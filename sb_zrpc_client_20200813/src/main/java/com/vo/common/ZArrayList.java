package com.vo.common;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 
 * add或者构造时参数可null的ArrayList
 * 
 * @author zhangzhen
 * @date 2021-12-29 22:42:21
 * 
 */
// com.vo.common.ZArrayList
public class ZArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;
	
	public ZArrayList(final E... e) {
		if (Objects.isNull(e)) {
			return;
		}
		for (final E e2 : e) {
			this.add(e2);
		}
	}
}
