package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.vo.aspect.ZDistributedTransactionAspect;
import com.vo.common.ZBeanFactoryPostProcessor;
import com.vo.common.ZRPCInstantiationAwareBeanPostProcessor;
import com.vo.conf.ZDTConf;
import com.vo.conf.ZrpcConfiguration;
import com.vo.netty.ZRCACA;
import com.vo.netty.ZRCRA1;
import com.vo.netty.ZRPCClientHandlerAdapter;

/**
 *
 *
 * @author zhangzhen
 * @date 2021-12-13 21:02:55
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Import(value = {
		ZrpcConfiguration.class,
		ZRCACA.class,
		ZRPCClientHandlerAdapter.class, ZRPCInstantiationAwareBeanPostProcessor.class,
		ZBeanFactoryPostProcessor.class, ZRCRA1.class,
		ZDistributedTransactionAspect.class,
		ZDTConf.class
})
public @interface EnableZRPC {

}
