package com.vo.netty;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.aop.scope.ScopedProxyFactoryBean;

/**
 * 
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 * 
 */
public class ZRMethodCache {

	public static Map<String, Method> allMethod() {
		return methodMap;
	}
	
	public static Object getObject(final Method method) {
		return objectMap.get(method);
	}
	
	public static Method getMethod(final String methodName) {
		return methodMap.get(methodName);
	}

	// FIXME 2021-12-13 20:29:15 zhangzhen :  写这个方法，写详细一点，防止重载的方法导致混乱
	public static String generateMethodName(final Method method, final Object object) {
		final Class<?>[] parameterTypes = method.getParameterTypes();
		final List<String> ptList = Arrays.stream(parameterTypes).map(Class::getSimpleName)
				.collect(Collectors.toList());

		final String pts = ptList.stream().collect(Collectors.joining(MEHTOD_ARG_SEPARATOR));
		final String methodName = method.getName();
//		final String methodName = method.getName() + SEPARATOR + pts;
		methodMap.put(methodName, method);
		
		objectMap.put(method, object);
		
		return methodName;
	}

	private static final String SEPARATOR = "@";
	private static final String MEHTOD_ARG_SEPARATOR = "_";


	/**
	 * k=Method.getName@Method.getParameterTypes().class.getSimpleName 
	 * v=Method
	 */
	private static final Map<String, Method> methodMap = new ConcurrentHashMap<>();
	private static final Map<Method, Object> objectMap = new ConcurrentHashMap<>();
		
}
