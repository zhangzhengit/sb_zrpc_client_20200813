		
1、 新项目pom.xml引入		
		<dependency>
			<groupId>com.example</groupId>
			<artifactId>sb_zlog_20201217</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>

2、配置handler
	# 配置输出到控制台
	# 此handler名称
	zlog.console.name=CONSOLE
	# 此handler是否启用
	zlog.console.enable=true
	# 此handler的pattern，支持的占位符在 ZLPatternEnum 里
	zlog.console.pattern=%DATE_TIME [%LEVEL] [%THREAD]-[%CLASS_NAME::%METHOD@%LINE_NUMBER] : [%MESSAGE]
	# 此handler的具体handler类
	zlog.console.handler=com.vo.log.ZLogConsoleHandler
	
	# 配置输出到文件
	zlog.file.name=FILE
	zlog.file.enable=true
	zlog.file.pattern=%DATE_TIME [%LEVEL] [%THREAD]-[%CLASS_NAME::%METHOD@%LINE_NUMBER] : [%MESSAGE]
	zlog.file.handler=com.vo.log.ZLogFileHandler
	# 要输出的文件路径
	zlog.file.filepath=/Users/zhangzhen/zossUpload/png/zlog_test.log
	
	# 配置输出到httpapi
	zlog.http.name=HTTP
	zlog.http.enable=false
	zlog.http.pattern=%DATE_TIME [%LEVEL] [%THREAD]-[%CLASS_NAME::%METHOD@%LINE_NUMBER] : [%MESSAGE]
	zlog.http.handler=com.vo.log.ZLogHttpHandler
	# POST到的httpapi
	zlog.http.httpAPI=http://ip:端口/log

3、	@EnableZLog

4、 	@Autowired ZLog log;

5、 log.xxx 输出日志

