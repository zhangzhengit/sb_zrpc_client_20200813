package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import com.vo.async.DefaultZRPCAsyncAfterReturn;
import com.vo.async.ZRPCAsyncAfterReturn;

/**
 * 	标记在@ZRPCRemoteMethodAnnotation里的方法上，表示这个方法异步执行。
 * 	注意：带有此注解的远程方法调用方的方法上，void仅表示在调用方不接收返回值，并不表示对应的远程方法
  *    提供方必须void，因为是异步执行，所以可以先返回然后等待远程方法返回结果后再执行之后逻辑，所以调用方可以
  *    使用void
 *
 * @author zhangzhen
 * @date 2021-12-23 17:02:26
 *
 */
// FIXME 2021-12-23 18:47:25 zhangzhen :  暂时只支持异步的远方方法返回为String类型，
// TODO 支持任意类型，client 接收时根据handle方法的参数来zpu.des
@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZRPCAsync {

	/**
	 * 方法提供方返回结果后，需要继续执行的操作，按数组顺序依次执行，上一次的返回结果作为下一次的参数
	 *
	 * @return
	 */
	Class<? extends ZRPCAsyncAfterReturn>[] afterReturn() default {DefaultZRPCAsyncAfterReturn.class};

}
