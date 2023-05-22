package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * zrpc-client. 表示远程方法的调用方,用在接口上，表示这个接口里的所有方法都去调用远程方法
 *
 * @author zhangzhen
 * @date 2021-12-10 18:06:08
 *
 */
@Documented
@Lazy
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
// FIXME 2021-12-29 23:32:45 zhangzhen :  todo 支持可变参数(数组)
public @interface ZRPCRemoteMethodAnnotation {

	/**
	 * 要调用的服务名称
	 *
	 * @return
	 */
	String serviceName();

}
