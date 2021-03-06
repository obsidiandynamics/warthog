package com.obsidiandynamics.warthog;

import java.security.*;

import javax.net.ssl.*;

import org.apache.http.config.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.impl.nio.conn.*;
import org.apache.http.impl.nio.reactor.*;
import org.apache.http.nio.conn.*;
import org.apache.http.nio.conn.ssl.*;
import org.apache.http.nio.reactor.*;

public final class HttpClient {
  private static final int TIMEOUT_MILLIS = 30_000;

  private static final int POOL_SIZE = 8;
  
  private HttpClient() {}
  
  public static CloseableHttpAsyncClient create() throws IOReactorException, NoSuchAlgorithmException {
    final var sessionStrategy = RegistryBuilder
        .<SchemeIOSessionStrategy>create()
        .register("http", NoopIOSessionStrategy.INSTANCE)
        .register("https", new SSLIOSessionStrategy(SSLContext.getDefault(), new DefaultHostnameVerifier()))
        .build();

    final var selectInterval = Math.min(1_000, TIMEOUT_MILLIS);
    final var ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.custom()
                                                         .setSelectInterval(selectInterval)
                                                         .setSoTimeout(TIMEOUT_MILLIS)
                                                         .setConnectTimeout(TIMEOUT_MILLIS)
                                                         .build());
    final var connectionManager = new PoolingNHttpClientConnectionManager(ioReactor, sessionStrategy);
    connectionManager.setMaxTotal(POOL_SIZE);
    connectionManager.setDefaultMaxPerRoute(POOL_SIZE);

    final var client = HttpAsyncClients.custom()
        .setConnectionManager(connectionManager)
        .build();
    client.start();
    return client;
  }
}
