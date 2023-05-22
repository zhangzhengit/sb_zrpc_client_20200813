package com.vo.async;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.vo.cache.ZMethodAysncResultCache;
import com.vo.cache.ZMethodAysncResultCache.ZMethodInfo;
import com.vo.common.ZRPCRemoteMethodAnnotationBeanGenerator;
import com.vo.common.ZProtobufUtil;
import com.vo.netty.ZRPCProtocol;

/**
 * ZRPCAsyncAfterReturn 的 Runnable对象
 * 
 * @author zhangzhen
 * @date 2021-12-23 18:59:41
 * 
 */
public class ZRPCAsyncAfterReturnCallable implements Callable<Object>{

	private final ZRPCProtocol zrpe;

	public ZRPCAsyncAfterReturnCallable(final ZRPCProtocol zrpe) {
		this.zrpe = zrpe;
	}
	
	@Override
	public Object call() throws Exception {
		
		final Object rv = zrpe.getRv();
		if (Objects.isNull(rv)) {
			return null;
		}
		
		final ZMethodInfo zMethodInfo = ZMethodAysncResultCache.get(zrpe.getId());
		if (Objects.isNull(zMethodInfo)) {
			return null;
		}
		
		final String afterReturn = zMethodInfo.getAfterReturn();
		final List<String> arList = Lists.newArrayList(afterReturn.split(ZRPCRemoteMethodAnnotationBeanGenerator.AFTER_RETURN_DELIMITER));
		final AtomicReference<Object> rrrr = new AtomicReference<>();
		for (final String className : arList) {
			final Class<?> cls = Class.forName(className);
			final Optional<Method> handleMO = 
					Lists.newArrayList(cls.getDeclaredMethods())
						.stream()
						.filter(m -> m.getName().equals(ZRPCAsyncAfterReturn.HANDLE_METHOD_NAME))
						.findFirst();
			if (handleMO.isPresent()) {
				final Method hm = handleMO.get();
				
				if (Objects.isNull(rrrr.get())) {
					final String rJSON = ZProtobufUtil.deserialize((byte[]) zrpe.getRv(), String.class);
					final Object arr1 = hm.invoke(cls.newInstance(), rJSON);
					rrrr.set(arr1);
				} else {
					final Object arr2 = hm.invoke(cls.newInstance(), String.valueOf(rrrr.get()));
					rrrr.set(arr2);
				}
			}
		}
		
		return rrrr.get();
	}

}
