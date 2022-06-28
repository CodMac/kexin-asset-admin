package com.kexin.framework.security.filter;

import org.apache.commons.io.output.TeeOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class BufferedResponseWrapper implements HttpServletResponse {

    private final HttpServletResponse original;

    private TeeServletOutputStream tee;

    private ByteArrayOutputStream bos;

    public BufferedResponseWrapper(HttpServletResponse response) {
        original = response;
    }

    /**
     * This method is used to get the String content of ByteArrayOutputStream
     *
     * @return content
     */
    public String getContent() {
        return bos != null ? bos.toString() : null;
    }

    /**
     * This method uses HttpServletResponse class implementation.
     *
     * @return PrintWriter
     */
    @Override
    public PrintWriter getWriter() throws IOException {

        return original.getWriter();

    }

    /**
     * This method uses to get the outputStream copy for logging the response
     * body.
     *
     * @return {@link ServletOutputStream}
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (tee == null) {
            bos = new ByteArrayOutputStream();
            // creating two copy of ServletResponse outputStream
            tee = new TeeServletOutputStream(original.getOutputStream(), bos);
        }
        return tee;

    }

    @Override
    public String getCharacterEncoding() {

        return original.getCharacterEncoding();

    }

    @Override
    public String getContentType() {

        return original.getContentType();

    }

    @Override
    public void setCharacterEncoding(String charset) {
        original.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        original.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
    }

    @Override
    public void setContentType(String type) {
        original.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        original.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return original.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        tee.flush();
    }

    @Override
    public void resetBuffer() {
        original.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return original.isCommitted();
    }

    @Override
    public void reset() {
        original.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        original.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return original.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie) {
        original.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return original.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return original.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return original.encodeRedirectURL(url);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String encodeUrl(String url) {
        return original.encodeUrl(url);
    }

    @Deprecated
    @Override
    public String encodeRedirectUrl(String url) {
        return original.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        original.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        original.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        original.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        original.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        original.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        original.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        original.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        original.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        original.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        original.setStatus(sc);
    }

    @Deprecated
    @Override
    public void setStatus(int sc, String sm) {
        original.setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return original.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return original.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return original.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return original.getHeaderNames();
    }

    public static class TeeServletOutputStream extends ServletOutputStream {

        private final TeeOutputStream targetStream;

        /**
         * Initializing TeeOutputStream.
         *
         * @param one one
         * @param two two
         */
        public TeeServletOutputStream(OutputStream one, OutputStream two) {
            targetStream = new TeeOutputStream(one, two);
        }

        @Override
        public void write(int arg0) throws IOException {
            this.targetStream.write(arg0);
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            this.targetStream.flush();
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.targetStream.close();
        }
        @Override
        public boolean isReady() {
            return false;
        }
        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}
