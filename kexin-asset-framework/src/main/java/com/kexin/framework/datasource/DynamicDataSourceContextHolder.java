package com.kexin.framework.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source switching
 */
public class DynamicDataSourceContextHolder {
    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    public static void setDataSourceType(String dsType) {
        THREAD_LOCAL.set(dsType);
        log.info("Switch with data source {}", dsType);
    }

    /**
     * Get the key for the current thread data source
     */
    public static String getDataSourceKey() {
        return THREAD_LOCAL.get();
    }

    /**
     * Clear the key for the current thread data source
     */
    public static void clearDataSourceKey() {
        THREAD_LOCAL.remove();
    }
}
