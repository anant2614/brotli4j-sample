/*
 * Copyright (c) 2016 MeteoGroup Deutschland GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.glance;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class HelloBrotliHttpController {

  @RequestMapping(value = "/hello", method = GET)
  @ResponseBody
  RespMsg helloBrotli() {
    return new RespMsg("success!");
  }

  @RequestMapping(value = "/decompress", method = GET)
  @ResponseBody
  String decompressBrotli() throws IOException {
    return run("http://localhost:8080/hello");
  }

  public String run(String url) throws IOException {

    OkHttpClient client = new OkHttpClient.Builder()
//            .addInterceptor(new GzipInterceptor())
            .addInterceptor(new BrotliInterceptor())
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build();
    MediaType JSON = MediaType.parse("application/json");
    String json = "{\"userId\":\"c6503bd6f4c16f0a\",\"sessionId\":\"4d3a8e1f-0d06-4ccc-bd0e-c9f0e1b28306\",\"glanceIdsWithTimestamp\":[{\"glanceId\":\"AYPQHzEr\",\"servedAtInSecs\":1665653056}]}";
    RequestBody requestBody = RequestBody.create(json, JSON);
    Request request = new Request.Builder().url(url)
            .header("x-api-key", "30aedfec48ddd7c42cb8cd855b431a774a0d6b17")
            .header("x-gl-trace", "true")
//            .post(requestBody)
            .get()
            .tag("req")
            .build();

    try (Response response = client.newCall(request).execute()) {
      System.out.println("Response ->>>> " + response);
      return response.body().string();
    }
  }
}
