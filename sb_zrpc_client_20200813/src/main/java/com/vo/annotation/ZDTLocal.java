package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ZDT 远程方法用的注解，拦截此注解，进行编程式事务控制
 *
 * @author zhangzhen
 * @date 2023年5月31日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZDTLocal {

}
