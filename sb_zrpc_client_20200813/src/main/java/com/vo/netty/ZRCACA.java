package com.vo.netty;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vo.annotation.EnableZRPC;
import com.vo.annotation.ZRPCComponent;
import com.vo.common.ZBeanFactoryPostProcessor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;

/**
 * 
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 * 
 */
@Component
public class ZRCACA implements ApplicationContextAware {

	private static final String JAVA_LANG_OBJECT = "java.lang.Object";
	private static ApplicationContext ac1;
	
	public static Object getBean(final Class className) {
		final Object bean = ac1.getBean(className);
		return bean;
	}
	public static Object getBean(final String beanName) {
		final Object bean = ac1.getBean(beanName);
		return bean;
	}
	@Override
	public void setApplicationContext(final ApplicationContext ac) throws BeansException {
		System.out.println(Thread.currentThread().getName() + "\t" + LocalDateTime.now() + "\t"
				+ "ZRCACA.setApplicationContext()");
		System.out.println();
		ac1 = ac;
		
		final boolean enableZRPC = ZBeanFactoryPostProcessor.isEnableZRPC();
		if (!enableZRPC) {
			return;
		}
		
		// FIXME 2021-12-22 4:59:18 zhangzhen :  这里处理，producer中methodname + 参数名 不可以重复，
		// 具体规则参照java重载方法的规则, 不导致混乱就可以

		
		
		final Map<String, Object> zMap = ac.getBeansWithAnnotation(ZRPCComponent.class);
		
		if (CollUtil.isEmpty(zMap)) {
			System.out.println("==================没有使用@" + ZRPCComponent.class.getCanonicalName() + "的类");
			return;
		}

		System.out.println("==================使用@" + ZRPCComponent.class.getCanonicalName() + "的类总个数=" + zMap.size());
		final Set<Entry<String, Object>> e = zMap.entrySet();
		int i = 1;
		for (final Entry<String, Object> entry : e) {
			System.out.println("\t" + i + "\t" + "beanName = " + entry.getKey());
			i++;
		}

		System.out.println("==================使用@" + ZRPCComponent.class.getCanonicalName() + "的类总个数=" + zMap.size());

		// TODO 暂时只简单判断所有@ZRPCComponent的类里的所有方法都不可以名称重复，这样做比较简单
		// 先不判断是否远程方法，如toString equals等等
		
		final int allMethodCount = e.stream()
						 .mapToInt(e1 -> {
							 final Object c = e1.getValue();
							 final Class<? extends Object> cls = c.getClass();
							 final Method[] dm = cls.getMethods();
							 final List<Method> selfMethodList = Lists.newArrayList(dm).stream()
									 	.filter(m -> !OBJECT_METHOD_SET.contains(m.getName()))
									 	.collect(Collectors.toList());
							 return selfMethodList.size();
						 }).sum();
		final long allMethodDistinctNameCount = e.stream()
				.flatMap(e1 -> {
					final Object c = e1.getValue();
					final Class<? extends Object> cls = c.getClass();
					final Method[] dm = cls.getMethods();
					final List<Method> selfMethodList = Lists.newArrayList(dm).stream()
							 	.filter(m -> !OBJECT_METHOD_SET.contains(m.getName()))
							 	.collect(Collectors.toList());
					return selfMethodList.stream();
				}).map(Method::getName)
				.distinct()
				.count();
		
		if (allMethodCount != allMethodDistinctNameCount) {
			// FIXME 2021-12-30 0:10:20 zhangzhen :  提示出具体哪些重复了
			throw new IllegalArgumentException(
					"==================使用 @" + ZRPCComponent.class.getCanonicalName() + " 的类中有名称重复的方法");
		}
		
		for (final Entry<String, Object> entry : e) {
			final Object zrpcComponent = entry.getValue();
			final Class<? extends Object> cls = zrpcComponent.getClass();
			final Method[] methodArray = cls.getMethods();

			if (ArrayUtil.isEmpty(methodArray)) {
				continue;
			}

			for (final Method method : methodArray) {
				if (OBJECT_METHOD_SET.contains(method.getName())) {
					continue;
				}
				ZRMethodCache.generateMethodName(method, zrpcComponent);
			}
		}

	}

	public static final ImmutableSet<String> OBJECT_METHOD_SET;

	static {
		final Set<String> set = Sets.newHashSet();
		set.add("getClass");
		set.add("hashCode");
		set.add("equals");
		set.add("toString");
		set.add("notify");
		set.add("notifyAll");
		set.add("wait");
		
		OBJECT_METHOD_SET = ImmutableSet.copyOf(set);
	}
	
	public static boolean checkSuperclassISZRC(final Object zrcObject) {
		final Class<?> superclass = zrcObject.getClass().getSuperclass();
		if (superclass.getCanonicalName().equals(JAVA_LANG_OBJECT)) {
			return true;
		}

		final ZRPCComponent a = superclass.getDeclaredAnnotation(ZRPCComponent.class);
		if (Objects.nonNull(a)) {
			return true;
		}

		return false;
	}
	
}
