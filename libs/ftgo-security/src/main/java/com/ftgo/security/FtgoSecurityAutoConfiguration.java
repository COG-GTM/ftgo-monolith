package com.ftgo.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnClass({EnableWebSecurity.class, SecurityFilterChain.class})
@ConditionalOnMissingBean(SecurityFilterChain.class)
@Import(FtgoSecurityConfiguration.class)
public class FtgoSecurityAutoConfiguration {
}
