package com.vo.cache;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.vo.async.ZRPCAsyncAfterReturn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * 
 * @author zhangzhen
 * @date 2021-12-23 17:55:21
 * 
 */
public class ZMethodAysncResultCache {

	/**
	 * <请求ID,方法相关信息>
	 */
	private static final ConcurrentMap<String, ZMethodInfo> map = Maps.newConcurrentMap();

	public static void put(final String id, final ZMethodInfo zMethodInfo) {
		map.put(id, zMethodInfo);
	}

	public static ZMethodInfo get(final String id) {
		final ZMethodInfo m = map.get(id);
		return m;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ZMethodInfo{
		private String requestId;
		private String name;
		private boolean isVoid;
		private boolean isAsync;
		private String afterReturn;
	}
	
}
