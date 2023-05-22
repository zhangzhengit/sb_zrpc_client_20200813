package com.vo.conf;

import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 *
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "zrpc")
public class ZrpcConfiguration {

	@NotEmpty
	private String serverHost;

	@NotNull
	private Integer serverPort;

	@NotEmpty
	private Set<String> scanPackageNameSet = Sets.newHashSet("com");

	@NotEmpty
	private String serviceName;

}
