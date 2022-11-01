
package com.example.glance;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.Encoder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * BrotliServletFilter applies brotli compression to servlet response data.
 * This filter will only activate itself, when the request contains an 'Accept-Encoding' header
 * and this header contains a content type value 'br'.
 * <br>
 * <br>
 * This filter accepts init parameters and uses the following defaults:
 * <pre>
 *   brotli.compression.parameter.mode=generic       [generic, text, font]
 *   brotli.compression.parameter.quality=5          [0..11]
 *   brotli.compression.parameter.lgwin=22           [10..24]
 *   brotli.compression.parameter.lgblock=0          [16..24]
 * </pre>
 */
@Component
@Order(1)
@WebFilter(urlPatterns = "/*", initParams = {
        @WebInitParam(name = "brotli.compression.parameter.quality", value = "5"),    //  [0..11]
        //@WebInitParam(name = "brotli.compression.parameter.mode", value = "generic"), //  [generic, text, font]
        //@WebInitParam(name = "brotli.compression.parameter.lgwin", value = "22"),     //  [10..24]
        //@WebInitParam(name = "brotli.compression.parameter.lgblock", value = "0"),    //  [16..24]
})
public class BrotliServletFilter implements Filter {

  /**
   * As defined in RFC draft "Brotli Compressed Data Format".
   *
   * @see <a href="http://www.ietf.org/id/draft-alakuijala-brotli-08.txt"></a>
   */
  public static final String BROTLI_HTTP_CONTENT_CODING = "br";

  /**
   * Name of the {@link BrotliServletFilter} init parameter.
   */
  public static final String BROTLI_COMPRESSION_PARAMETER_MODE = "brotli.compression.parameter.mode";

  /**
   * Name of the {@link BrotliServletFilter} init parameter.
   */
  public static final String BROTLI_COMPRESSION_PARAMETER_QUALITY = "brotli.compression.parameter.quality";

  /**
   * Name of the {@link BrotliServletFilter} init parameter.
   */
  public static final String BROTLI_COMPRESSION_PARAMETER_LGWIN = "brotli.compression.parameter.lgwin";

  /**
   * Name of the {@link BrotliServletFilter} init parameter.
   */
  public static final String BROTLI_COMPRESSION_PARAMETER_LGBLOCK = "brotli.compression.parameter.lgblock";

  private static final HttpAcceptEncodingParser
                           ACCEPT_ENCODING_PARSER                    = new HttpAcceptEncodingParser();
  private static final int DEFAULT_BROTLI_SERVLET_COMPRESSION_QUALITY = 5;

  protected Encoder.Parameters brotliCompressionParameters = new Encoder.Parameters();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {
        Brotli4jLoader.ensureAvailability();
    } catch (UnsatisfiedLinkError | IllegalStateException | SecurityException e) {
      throw new ServletException(e);
    }
    applyFilterConfig(filterConfig);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    //handle Brotli encoding
    if (ACCEPT_ENCODING_PARSER.acceptBrotliEncoding((HttpServletRequest) request)) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.addHeader("Content-Encoding", BROTLI_HTTP_CONTENT_CODING);
      BrotliServletResponseWrapper
              brotliResponse = new BrotliServletResponseWrapper(httpResponse, brotliCompressionParameters);
      try {
        chain.doFilter(request, brotliResponse);
      } finally {
        brotliResponse.close();
      }
    } else {
      chain.doFilter(request, response);
    }
  }


  private void applyFilterConfig(FilterConfig filterConfig) {
    brotliCompressionParameters
        .setMode(getInitParameterAsBrotliMode(filterConfig, BROTLI_COMPRESSION_PARAMETER_MODE,
                Encoder.Mode.GENERIC))
        .setQuality(getInitParameterAsInteger(filterConfig, BROTLI_COMPRESSION_PARAMETER_QUALITY,
                DEFAULT_BROTLI_SERVLET_COMPRESSION_QUALITY))
        .setWindow(getInitParameterAsInteger(filterConfig, BROTLI_COMPRESSION_PARAMETER_LGWIN,
                Encoder.Parameters.DEFAULT.lgwin()));
  }

  private Encoder.Mode getInitParameterAsBrotliMode(FilterConfig filterConfig,
          String parameterName, Encoder.Mode defaultValue) {
    String initParameter = filterConfig.getInitParameter(parameterName);
    if (null != initParameter && !initParameter.trim().isEmpty()) {
      return Encoder.Mode.valueOf(initParameter.toUpperCase());
    }
    return defaultValue;
  }

  private int getInitParameterAsInteger(FilterConfig filterConfig, String parameterName, int defaultValue) {
    String initParameter = filterConfig.getInitParameter(parameterName);
    if (null != initParameter && !initParameter.trim().isEmpty()) {
      return Integer.parseInt(initParameter);
    }
    return defaultValue;
  }

}
