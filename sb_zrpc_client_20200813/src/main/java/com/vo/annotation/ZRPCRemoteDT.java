package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 用在@ZRPCRemoteMethodAnnotation标记的interface里面的方法上,
 * 表示此方法是一个分布式事务中的一步
 *
 * @author zhangzhen
 * @date 2023年5月31日
 *
 */
@Documented
@Lazy
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZRPCRemoteDT {

}
