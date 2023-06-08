package com.vo.aspect;

import lombok.AllArgsConstructor;

/**
 *
 *	用于抛出异常传值
 * @author zhangzhen
 * @date 2023年5月31日
 *
 */
@AllArgsConstructor
public class ZDTE extends RuntimeException{

	private static final long serialVersionUID = 1L;

	private final Object object;

	public Object getObject() {
		return this.object;
	}

}
