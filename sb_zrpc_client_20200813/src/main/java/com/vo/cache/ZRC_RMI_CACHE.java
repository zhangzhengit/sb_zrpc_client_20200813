package com.vo.cache;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.vo.netty.ZRPCProtocol;

/**
 * 执行结果暂时存放
 *
 * @author zhangzhen
 * @data Aug 14, 2020
 *
 */
public class ZRC_RMI_CACHE {

	private final static ConcurrentMap<String, ZRPCProtocol> resultMap = Maps.newConcurrentMap();

	/**
	 * 存放方法执行结果
	 *
	 * @param requestId
	 * @param zrpe
	 */
	public static void setResult(final String requestId, final ZRPCProtocol zrpe) {
		resultMap.put(requestId, zrpe);
	}

	/**
	 * 取出方法执行结果
	 *
	 * @param requestId
	 * @return
	 */
	public static ZRPCProtocol getResultUntilReturn(final String requestId) {
		while (true) {
			final ZRPCProtocol r = resultMap.get(requestId);
			if (Objects.isNull(r)) {
				continue;
			}
			resultMap.remove(requestId);

			return r;
		}
	}

	public static String gKeyword(final ZRPCProtocol zrpe) {
		final String keyword = zrpe.getName() + "@" + zrpe.getId();
		return keyword;
	}


}
