package com.vo.common;

import cn.hutool.core.lang.UUID;

/**
 *  
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 * 
 */
public class ZIDG {

	public static String generateId() {
		final String id = UUID.randomUUID().toString();
		return id;
	}
}
