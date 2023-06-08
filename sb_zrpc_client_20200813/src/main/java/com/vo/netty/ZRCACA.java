package com.vo.netty;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vo.annotation.EnableZRPC;
import com.vo.annotation.ZRPCComponent;
import com.vo.common.ZBeanFactoryPostProcessor;
import com.vo.core.ZLog2;

import cn.hutool.core.collection.CollUtil;

/**
 *
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 *
 */
@Component
public class ZRCACA implements ApplicationContextAware {

	private static final ZLog2 LOG = ZLog2.getInstance();

	private static final String JAVA_LANG_OBJECT = "java.lang.Object";
	private static ApplicationContext ac1;

	public static Object getBean(final Class className) {
		final Object bean = ZRCACA.ac1.getBean(className);
		return bean;
	}
	public static Object getBean(final String beanName) {
		final Object bean = ZRCACA.ac1.getBean(beanName);
		return bean;
	}
	/**
	 *
	 */
	@Override
	public void setApplicationContext(final ApplicationContext ac) throws BeansException {
		LOG.info("ZRPC-执行");

		ZRCACA.ac1 = ac;

		final boolean enableZRPC = ZBeanFactoryPostProcessor.isEnableZRPC();
		if (!enableZRPC) {
			LOG.info("ZRPC-{}未启用,return",EnableZRPC.class.getCanonicalName());
			return;
		}

		// TODO  只简单处理为了方法名：bean中method.getName()不允许重复

		final Map<String, Object> zMap = ac.getBeansWithAnnotation(ZRPCComponent.class);

		if (CollUtil.isEmpty(zMap)) {
			LOG.info("ZRPC-没有@{}的bean,return",ZRPCComponent.class.getCanonicalName());
			return;
		}

		LOG.info("ZRPC-@{}的bean的个数={}",ZRPCComponent.class.getCanonicalName(),zMap.size());

		final Set<Entry<String, Object>> e = zMap.entrySet();
		int i = 1;
		for (final Entry<String, Object> entry : e) {
			System.out.println("\t" + i + "\t" + "beanName = " + entry.getKey());
			i++;
		}

		final Set<String> zrpcBeanMethodNameSet = Sets.newHashSet();
		for (final Entry<String, Object> entry : e) {
			final Object zrpcBean = entry.getValue();

			final Class<? extends Object> class1 = zrpcBean.getClass();
			final String javaClassName = gCN(class1.getCanonicalName());
			final Class<?> cls = forName(javaClassName);
			final Method[] declaredMethods = cls.getDeclaredMethods();
			for (final Method m : declaredMethods) {

				final String beanName = entry.getKey();

				LOG.info("ZRPC-beanName={},methodName={}", beanName, m.getName());

				final String methodName = m.getName();
//				final String bean_methodName = beanName + "@" + m.getName();
				final boolean add = zrpcBeanMethodNameSet.add(methodName);

				if (!add) {
					final String message = "使用 @" + ZRPCComponent.class.getCanonicalName() + " 的bean中有名称重复的方法,bean@方法名 = "
							+ beanName + "@" + methodName;
					throw new IllegalArgumentException(message);
				}

				if (ZRCACA.OBJECT_METHOD_SET.contains(m.getName())) {
					continue;
				}

				// 返回值不允许基本类型
				if (ZRCACA.BASIC_TYPE_SET.contains(m.getReturnType().getName())) {
					throw new IllegalArgumentException("方法:[" + m.getName() + "]返回值不允许为基本类型,请替换为包装类型");
				}

				ZRMethodCache.generateMethodName(m, zrpcBean);
			}

		}

		LOG.info("ZRPC-@{}解析完成", ZRPCComponent.class.getCanonicalName());

	}

	private static Class<?> forName(final String javaClassName) {
		try {
			return Class.forName(javaClassName);
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final ImmutableSet<String> OBJECT_METHOD_SET;
	public static final ImmutableSet<String> BASIC_TYPE_SET;

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


		final Set<String> basicSet = Sets.newHashSet();
		basicSet.add("byte");
		basicSet.add("short");
		basicSet.add("int");
		basicSet.add("long");
		basicSet.add("char");
		basicSet.add("boolean");
		basicSet.add("float");
		basicSet.add("double");

		BASIC_TYPE_SET = ImmutableSet.copyOf(basicSet);
	}

	public static boolean checkSuperclassISZRC(final Object zrcObject) {
		final Class<?> superclass = zrcObject.getClass().getSuperclass();
		if (ZRCACA.JAVA_LANG_OBJECT.equals(superclass.getCanonicalName())) {
			return true;
		}

		final ZRPCComponent a = superclass.getDeclaredAnnotation(ZRPCComponent.class);
		if (Objects.nonNull(a)) {
			return true;
		}

		return false;
	}

	private static String gCN(final String beanNameClassName) {
//		com.vo.test.p.RC2_P$$EnhancerBySpringCGLIB$$c04bd17b

		final String k = "$$";
		final int i = beanNameClassName.indexOf(k);
		if (i <= -1) {
			return beanNameClassName;
		}
		final String cn = beanNameClassName.substring(0,i);

		return cn;
	}
}
