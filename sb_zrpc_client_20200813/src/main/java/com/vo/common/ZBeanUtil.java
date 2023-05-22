package com.vo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 
 * 
 * @author zhangzhen
 * @date 2021-12-14 22:23:56
 * 
 */
public class ZBeanUtil {

	private static ConfigurableListableBeanFactory beanFactory1;
	private static final Map<String, Object> beanmap = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Object> classmap = new ConcurrentHashMap<>();

	public static void setBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
		beanFactory1 = beanFactory;
	}
	
	public static void test_name() {
	}

	public static <T> T getBean(final Class<T> className) {
		final Object v = classmap.get(className);
		if (v != null) {
			return (T) v;
		}
		final Object bean = beanFactory1.getBean(className);
		classmap.put(className, bean);
		return (T) bean;
	}

	public static <T> T getBean(final String beanName) {
		final Object v = beanmap.get(beanName);
		if (v != null) {
			return (T) v;
		}

		final Object bean = beanFactory1.getBean(beanName);
		beanmap.put(beanName, bean);
		
		return (T) bean;
	}

}
