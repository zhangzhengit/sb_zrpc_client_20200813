package com.vo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用在bean的方法上，表示此方法启用分布式事务.
 * 如 :
 *  @ZDistributedTransaction
 *  public void a(){
 *  	r.r1();
 *  	r.r2();
 *  	r.r3();
 *  }
 * 	R r;是@ZRPCRemoteMethodAnnotation接口，r1 r2 r3都是远程方法，
 *  都放在方法a()里，加入@ZDistributedTransaction，表示
 *  三个方法在一个分布式事务里执行.
 *
 *
 * @author zhangzhen
 * @date 2023年5月31日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZDistributedTransaction {

	/**
	 * 分布式事务的唯一ID，用来区分唯一的一个分布式事务，需要在一个分布式事务里执行的多个方法上，都用一个相同的ID
	 *
	 * @return
	 */
	String id();

	/**
	 * 各个步骤的方法名称
	 *
	 * @return
	 *
	 */
	String[] name();

}
