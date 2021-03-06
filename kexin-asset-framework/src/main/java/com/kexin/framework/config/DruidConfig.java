package com.kexin.framework.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.kexin.common.enums.DataSourceType;
import com.kexin.common.utils.spring.SpringUtils;
import com.kexin.framework.config.properties.DruidDataSourceProperties;
import com.kexin.framework.datasource.DynamicDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * druid data source configuration
 */
@Configuration
public class DruidConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource(DruidDataSourceProperties druidDataSourceProperties) {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidDataSourceProperties.dataSource(dataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource(DruidDataSourceProperties druidDataSourceProperties) {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidDataSourceProperties.dataSource(dataSource);
    }

    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(DataSource masterDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);
        setDataSource(targetDataSources, DataSourceType.SLAVE.name(), "slaveDataSource");
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }

    public void setDataSource(Map<Object, Object> targetDataSources, String sourceName, String beanName) {
        try {
            DataSource dataSource = SpringUtils.getBean(beanName);
            targetDataSources.put(sourceName, dataSource);
        } catch (Exception ignored) {
        }
    }

//    /**
//     * ?????????????????????????????????
//     */
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Bean
//    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
//    public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties) {
//        // ??????web?????????????????????
//        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
//        // ??????common.js???????????????
//        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
//        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
//        final String filePath = "support/http/resources/js/common.js";
//        // ??????filter????????????
//        Filter filter = new Filter() {
//            @Override
//            public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
//            }
//
//            @Override
//            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//                    throws IOException, ServletException {
//                chain.doFilter(request, response);
//                // ??????????????????????????????????????????
//                response.resetBuffer();
//                // ??????common.js
//                String text = Utils.readFromResource(filePath);
//                // ????????????banner, ???????????????????????????
//                text = text.replaceAll("<a.*?banner\"></a><br/>", "");
//                text = text.replaceAll("powered.*?shrek.wang</a>", "");
//                response.getWriter().write(text);
//            }
//
//            @Override
//            public void destroy() {
//            }
//        };
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setFilter(filter);
//        registrationBean.addUrlPatterns(commonJsPattern);
//        return registrationBean;
//    }
}
