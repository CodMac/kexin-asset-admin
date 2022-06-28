package com.kexin.framework.security.filter;

import org.springframework.util.CollectionUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class BufferedRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> params = new HashMap<>();

    private final byte[] buffer;

    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }

    @Override
    public String getParameter(String name) {
        if (params.get(name) != null && params.get(name).length >= 1) {
            return params.get(name)[0];
        }
        return super.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(params.keySet());
    }

    @Override
    public String[] getParameterValues(final String name) {
        return params.get(name);
    }

    /**
     * Initializing {@link InputStream#}
     *
     * @param req request
     */
    public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        if (!CollectionUtils.isEmpty(req.getParameterMap())) {
            this.params.putAll(req.getParameterMap());
        }

        // Read InputStream and store its content in a buffer.
        InputStream in = req.getInputStream();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int letti;
        while ((letti = in.read(buf)) > 0) {
            arrayOutputStream.write(buf, 0, letti);
        }
        this.buffer = arrayOutputStream.toByteArray();
    }

    /**
     * This method is used for fetching the inputStream from request.
     *
     * @return in
     */
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.buffer);
        return new BufferedServletInputStream(bais);
    }


    /**
     * This method is used for fetching the request body.
     *
     * @return body
     * @throws IOException i/o exception
     */
    String getRequestBody() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
        String line;
        StringBuilder inputBuffer = new StringBuilder();
        do {
            line = reader.readLine();
            if (null != line) {
                inputBuffer.append(line.trim());
            }
        }
        while (line != null);
        reader.close();
        return inputBuffer.toString().trim();
    }

    private static final class BufferedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream in;

        public BufferedServletInputStream(ByteArrayInputStream inputStream) {
            this.in = inputStream;
        }

        @Override
        public int available() {
            return this.in.available();
        }

        @Override
        public int read() {
            return this.in.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return this.in.read(buf, off, len);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }
    }

}
