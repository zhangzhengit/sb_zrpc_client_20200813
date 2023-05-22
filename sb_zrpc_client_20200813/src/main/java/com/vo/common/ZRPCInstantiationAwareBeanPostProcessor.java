package com.vo.common;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

import com.vo.conf.ZrpcConfiguration;
import com.vo.netty.ZRPCClientHandlerAdapter;

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
		
		if (!g.get()) {
			System.out.println("configuration = " + configuration);
			System.out.println("G--------------------START");
			ZRPCRemoteMethodAnnotationBeanGenerator.generate(configuration);
			System.out.println("G--------------------OK");
		}

		g.set(true);
		
		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
	}
}
