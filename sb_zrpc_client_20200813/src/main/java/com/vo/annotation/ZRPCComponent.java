package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 *  表示远程方法的被调用方，用在类上，表示这个类里的所有public方法都作为远程方法.
 *  所以被此注解标注的类上尽量不要有其他方法，此类只作为远程方法提供者使用.
 * 	获取此类的方法使用的getMethods，获取本类以及父类的所有public方法
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 *
 */
@Component
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZRPCComponent {

}
