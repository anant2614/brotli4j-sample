package com.example.glance

import okhttp3.Response
import okhttp3.Interceptor

/**
 * Transparent Brotli response support.
 *
 * Adds Accept-Encoding: br to request and checks (and strips) for Content-Encoding: br in
 * responses.  n.b. this replaces the transparent gzip compression in BridgeInterceptor.
 */
class BrotliInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    return if (chain.request().header("Accept-Encoding") == null) {
      val request = chain.request().newBuilder()
        .header("Accept-Encoding", "br,gzip")
        .build()

      val response = chain.proceed(request)

      uncompress(response)
    } else {
      chain.proceed(chain.request())
    }
  }
}

