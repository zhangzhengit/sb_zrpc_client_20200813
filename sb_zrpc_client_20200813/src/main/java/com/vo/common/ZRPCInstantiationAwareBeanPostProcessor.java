package com.vo.common;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration;
import org.springframework.stereotype.Component;

import com.vo.annotation.ZRPCAsync;
import com.vo.annotation.ZRPCRemoteMethodAnnotation;
import com.vo.conf.ZrpcConfiguration;
import com.vo.netty.ZRPCClientHandlerAdapter;

import groovyjarjarantlr.FileLineFormatter;

/**
 * 	在postProcessAfterInstantiation里面给@Autowired标注的interface动态生成实现类并注入到容器
 *
 * @author zhangzhen
 * @date 2021-12-30 16:58:19
 *
 */
@Component
public class ZRPCInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor{

	@Autowired
	ZRPCClientHandlerAdapter zrpcClientHandlerAdapter ;
	@Autowired
	private ZrpcConfiguration configuration;

	private final AtomicBoolean g = new AtomicBoolean();

	@Override
	public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {

		if (!this.g.get()) {
			System.out.println("configuration = " + this.configuration);
			System.out.println("G--------------------START");
			ZRPCRemoteMethodAnnotationBeanGenerator.generate(this.configuration);
			System.out.println("G--------------------OK");


			// FIXME 2023年6月1日 下午3:17:18 zhanghen: ZRPCASync
			final ConfigurableListableBeanFactory bf = ZBeanFactoryPostProcessor.getBeanFactory();

			final Map<String, Object> remoteMethodMap = bf.getBeansWithAnnotation(ZRPCRemoteMethodAnnotation.class);
			System.out.println("remoteMethodMap.size = " + remoteMethodMap.size());
			final Set<String> keySet = remoteMethodMap.keySet();

			System.out.println("remoteMethodMap = " + keySet);

			final Set<Entry<String, Object>> es = remoteMethodMap.entrySet();
			for (final Entry<String, Object> entry : es) {
				final Object rmBean = entry.getValue();
				System.out.println("rmBean = " + rmBean.getClass().getName());

				final List<Method> asyncMethodList = Arrays.asList(rmBean.getClass().getDeclaredMethods()).stream()
						.filter(m -> m.isAnnotationPresent(ZRPCAsync.class)).collect(Collectors.toList());

				for (final Method m : asyncMethodList) {

					System.out.println("asyncMethod = " + m.getName());
				}

			}


//		final Map<String, Object> asyncMap = bf.getBeansWithAnnotation(ZRPCAsync.class);
//		System.out.println("asyncMap.size = " + asyncMap.size());

		}

		this.g.set(true);


		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
	}
}
