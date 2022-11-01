
package com.example.glance;

import javax.servlet.http.HttpServletRequest;

import static java.lang.Float.parseFloat;

class HttpAcceptEncodingParser {

  private static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final String CODING_SEPARATOR = ",";
  private static final String CODING_QVALUE_SEPARATOR = ";";
  private static final String QVALUE_PREFIX = "q=";

  boolean acceptBrotliEncoding(HttpServletRequest httpRequest) {
    return acceptBrotliEncoding(httpRequest.getHeader(HTTP_HEADER_ACCEPT_ENCODING));
  }

  boolean acceptBrotliEncoding(String headerString) {
    if (null != headerString) {
      String[] weightedCodings = headerString.split(CODING_SEPARATOR, 0);
      for (String weightedCoding : weightedCodings) {
        String[] codingAndQvalue = weightedCoding.trim().split(CODING_QVALUE_SEPARATOR, 2);
        if (codingAndQvalue.length > 0) {
          if (BrotliServletFilter.BROTLI_HTTP_CONTENT_CODING.equals(codingAndQvalue[0].trim())) {
            if (codingAndQvalue.length == 1) {
              return true;
            } else {
              String qvalue = codingAndQvalue[1].trim();
              if (qvalue.startsWith(QVALUE_PREFIX)) {
                try {
                  return parseFloat(qvalue.substring(2).trim()) > 0;
                } catch (NumberFormatException e) {
                  return false;
                }
              }
            }
          }
        }
      }
    }
    return false;
  }
}
