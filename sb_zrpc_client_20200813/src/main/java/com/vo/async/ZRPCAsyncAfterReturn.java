package com.vo.async;

/**
 * 	表示@ZRPCAsync的方法的提供方返回值以后的操作
 * 
 * @author zhangzhen
 * @date 2021-12-23 17:39:05
 * 
 */
public interface ZRPCAsyncAfterReturn {
	
	String HANDLE_METHOD_NAME = "handle";
	
	/**
	 * @param json
	 * @return
	 */
	// FIXME 2021-12-23 18:52:58 zhangzhen :  暂时先支持json，后续支持任意类型然后使用protobuf，需与producer使用有共同字段的DTO传输
	Object handle(final String json) ;

}
