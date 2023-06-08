package com.vo.common;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import com.vo.annotation.EnableZRPC;
import com.vo.annotation.ZRPCRemoteMethodAnnotation;

import cn.hutool.core.collection.CollUtil;

/**
 *
 *
 * @author zhangzhen
 * @date 2021-12-10 20:58:48
 *
 */
@Component
public class ZBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private static ConfigurableListableBeanFactory beanFactory1;

	public static ConfigurableListableBeanFactory getBeanFactory() {
		return beanFactory1;
	}

	public static boolean isEnableZRPC() {

		return enableZRPC.get();

//		final Map<String, Object> e = getBeanFactory().getBeansWithAnnotation(EnableZRPC.class);
//		if (CollUtil.isEmpty(e)) {
//			return false;
//		}
//		return true;
	}

	public static Object getBean(final Class<?> className) {
		final Object bean = beanFactory1.getBean(className);
		return bean;
	}

	public static Object getBean(final String beanName) {
		final Object bean = beanFactory1.getBean(beanName);
		return bean;
	}

	@Override
	public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("ZBeanFactoryPostProcessor.postProcessBeanFactory()" + "\t" + LocalDateTime.now() + "\t"
				+ Thread.currentThread().getName());

		beanFactory1 = beanFactory;
		final Map<String, Object> e = beanFactory.getBeansWithAnnotation(EnableZRPC.class);
		enableZRPC.set(e.size() >= 0);
		ZBeanUtil.setBeanFactory(beanFactory);

	}

	private static final AtomicBoolean enableZRPC = new AtomicBoolean();

}
