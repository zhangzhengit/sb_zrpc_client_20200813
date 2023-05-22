package com.vo.async;

/**
 * 	默认实现
 * 
 * @author zhangzhen
 * @date 2021-12-23 17:50:15
 * 
 */
public final class DefaultZRPCAsyncAfterReturn implements ZRPCAsyncAfterReturn{

	@Override
	public Object handle(final String json) {
		return json;
	}

}
