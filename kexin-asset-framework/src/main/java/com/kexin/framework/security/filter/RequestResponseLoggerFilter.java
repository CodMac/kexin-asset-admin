package com.kexin.framework.security.filter;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;


public class RequestResponseLoggerFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggerFilter.class);
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    // url contains string "upload" or "export" no log output. example: (?!.*(upload|export)).*$
    private String regex = "";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (logger.isInfoEnabled()) {
            try {
                MDC.put("RequestId", generateUnique());
                HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                boolean isLogger = loggerFilter(httpServletRequest.getRequestURI());
                long startTime = System.currentTimeMillis();
                StringBuilder logMessage = (new StringBuilder("[")).append(httpServletRequest.getRemoteAddr()).append(" ").append(httpServletRequest.getMethod()).append(" ").append(httpServletRequest.getRequestURI()).append("]");
                String uuid = httpServletRequest.getHeader("serviceUUID");
                StringBuilder requestStrBuffer = new StringBuilder();
                StringBuilder responseStrBuffer = new StringBuilder();
                if (StringUtils.isEmpty(uuid)) {
                    requestStrBuffer.append("Receive request = ").append(logMessage).append(", cookie: ");
                    responseStrBuffer.append("Send response = ").append(logMessage).append(",cost: ");
                } else {
                    requestStrBuffer.append("Receive feign request = ").append(logMessage).append(", feign_uuid: ").append(uuid).append(", cookie: ");
                    responseStrBuffer.append("Send feign response = ").append(logMessage).append(", feign_uuid: ").append(uuid).append(",cost: ");
                }

                if (isLogger) {
                    BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpServletRequest);
                    BufferedResponseWrapper bufferedResponse = new BufferedResponseWrapper(httpServletResponse);
                    StringBuilder requestBody = new StringBuilder();
                    if (CONTENT_TYPE.equalsIgnoreCase(httpServletRequest.getContentType())) {
                        requestBody.append(JSON.toJSONString(bufferedRequest.getParameterMap()));
                    } else {
                        requestBody.append(bufferedRequest.getRequestBody());
                    }

                    requestBody = this.preRequestBodyConvert(httpServletRequest.getRequestURI(), requestBody);
                    requestStrBuffer.append(bufferedRequest.getHeader("cookie")).append(", Authorization: ").append(this.getAuthorization(bufferedRequest)).append(", body: ").append(requestBody);
                    logger.info(requestStrBuffer.toString());
                    filterChain.doFilter(bufferedRequest, bufferedResponse);
                    String responseBody = bufferedResponse.getContent();
                    long cost = System.currentTimeMillis() - startTime;
                    responseStrBuffer.append(cost).append("ms, ").append(", body: ").append(responseBody);
                } else {
                    requestStrBuffer.append(httpServletRequest.getHeader("cookie")).append(", Authorization: ").append(this.getAuthorization(httpServletRequest)).append(", body: XXX");
                    logger.info(requestStrBuffer.toString());
                    filterChain.doFilter(servletRequest, servletResponse);
                    long cost = System.currentTimeMillis() - startTime;
                    responseStrBuffer.append(cost).append("ms, ").append(", body:  XXX");
                }
                logger.info(responseStrBuffer.toString());
            } catch (Exception var22) {
                logger.error("Error occurred in logging the request and response", var22);
            } finally {
                MDC.remove("RequestId");
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() {

    }

    protected StringBuilder preRequestBodyConvert(String url, StringBuilder requestBody) {
        return requestBody;
    }

    protected String getAuthorization(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    /**
     * generate 17 bit +3 random bit code
     *
     * @return Request unique identification
     */
    public String generateUnique() {
        return StringUtils.join(DateFormatUtils.format(Calendar.getInstance()
                .getTime(), "yyyyMMddHHmmssSSS"), RandomStringUtils
                .randomNumeric(3));
    }

    public boolean loggerFilter(String url) {
        return (url != null && url.matches(regex));
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

}
