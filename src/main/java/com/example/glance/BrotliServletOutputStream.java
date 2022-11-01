
package com.example.glance;

import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BrotliServletOutputStream extends ServletOutputStream {

  private BrotliOutputStream brotliOutputStream;
  private HttpServletResponse   response;
  private final ByteArrayOutputStream buffer;
  private final Encoder.Parameters parameters;

  /**
   * @param outputStream outputStream.
   * @param parameters   brotli compression parameter
   */
  public BrotliServletOutputStream(OutputStream outputStream, Encoder.Parameters parameters,
          HttpServletResponse response) throws IOException {
    this.buffer = new ByteArrayOutputStream();
    this.response = response;
    this.parameters = parameters;
  }

  @Override
  public void write(int byteToWrite) throws IOException {
    this.buffer.write(byteToWrite);
  }

  @Override
  public void write(byte[] buffer) throws IOException {
    this.buffer.write(buffer);
  }

  @Override
  public void write(byte[] buffer, int offset, int len) throws IOException {
    this.buffer.write(buffer, offset, len);
  }

  @Override
  public void flush() throws IOException {
    this.buffer.flush();
  }

  @Override
  public void close() throws IOException {

    this.buffer.close();

    byte[] data = buffer.toByteArray();

    ByteArrayOutputStream baOut = new ByteArrayOutputStream();
    brotliOutputStream = new BrotliOutputStream(baOut, parameters);
    brotliOutputStream.write(data);
    brotliOutputStream.flush();
    byte[] result = baOut.toByteArray();
    response.setContentLength(result.length);
    OutputStream out = response.getOutputStream();
    out.write(result);
    out.flush();
    out.close();
    brotliOutputStream.close();
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
    throw new UnsupportedOperationException("WriteListener support is not yet implemented.");
  }
}
