
package com.example.glance;

import com.aayushatharva.brotli4j.encoder.Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class BrotliServletResponseWrapper extends HttpServletResponseWrapper {

  private BrotliServletOutputStream brotliServletOutputStream = null;
  private PrintWriter                                                       printWriter               = null;
  private final Encoder.Parameters brotliParameters;

  public BrotliServletResponseWrapper(HttpServletResponse response,
          Encoder.Parameters brotliCompressionParameters) throws IOException {
    super(response);
    brotliParameters = brotliCompressionParameters;
  }

  void close() throws IOException {
    if (this.printWriter != null) {
      this.printWriter.close();
    }
    if (this.brotliServletOutputStream != null) {
      this.brotliServletOutputStream.close();
    }
  }

  @Override
  public void flushBuffer() throws IOException {
    if (this.printWriter != null) {
      this.printWriter.flush();
    }
    try {
      if (this.brotliServletOutputStream != null) {
        this.brotliServletOutputStream.flush();
      }
    } finally {
      super.flushBuffer();
    }
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (this.printWriter != null) {
      throw new IllegalStateException("PrintWriter obtained already - cannot get OutputStream");
    }
    if (this.brotliServletOutputStream == null) {
      this.brotliServletOutputStream = new BrotliServletOutputStream(getResponse().getOutputStream(),
              brotliParameters, (HttpServletResponse) getResponse());
    }
    return this.brotliServletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (this.printWriter == null && this.brotliServletOutputStream != null) {
      throw new IllegalStateException("OutputStream obtained already - cannot get PrintWriter");
    }
    if (this.printWriter == null) {
      this.brotliServletOutputStream = new BrotliServletOutputStream(getResponse().getOutputStream(),
              brotliParameters, (HttpServletResponse) getResponse());
      this.printWriter = new PrintWriter(new OutputStreamWriter(this.brotliServletOutputStream,
              getResponse().getCharacterEncoding()));
    }
    return this.printWriter;
  }

  @Override
  public void setContentLength(int len) {
//    setContentLengthLong((long) len);
  }

  @Override
  public void setContentLengthLong(long len) {
    //ignore, since content length of compressed content does not match length of raw content.
//    this.getResponse().setContentLengthLong(len);
  }
}
