package com.kexin.framework.security.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class LoggerFilterConfig {

    @Value("${application.servlet-path:/}")
    public String urlPrefix;

    @Value("${filter.log.regex:}")
    public String loggerFilter;

    @Bean
    public FilterRegistrationBean<Filter> loggerFilterBean() {
        FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
        RequestResponseLoggerFilter filter = new RequestResponseLoggerFilter();
        filter.setRegex(loggerFilter);
        filterRegistration.setFilter(filter);
        filterRegistration.addUrlPatterns(urlPrefix + "*");
        return filterRegistration;
    }

}
