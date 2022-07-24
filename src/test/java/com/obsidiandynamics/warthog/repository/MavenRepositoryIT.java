package com.obsidiandynamics.warthog.repository;

import static org.junit.Assert.*;

import java.io.*;
import java.security.*;

import org.apache.http.impl.nio.client.*;
import org.apache.http.nio.reactor.*;
import org.fusesource.jansi.*;
import org.junit.*;

import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.repository.MavenRepository.*;

public final class MavenRepositoryIT {
  private static CloseableHttpAsyncClient httpClient;
  
  @BeforeClass
  public static void beforeClass() throws IOReactorException, NoSuchAlgorithmException {
    httpClient = HttpClient.create();
  }
  
  @AfterClass
  public static void afterClass() throws IOException {
    if (httpClient != null) {
      httpClient.close();
      httpClient = null;
    }
  }
  
  private static WarthogContext getContext() {
    return new WarthogContext(AnsiConsole.out, null, null, httpClient);
  }

  @Test
  public void testExistingPackage() throws Exception {
    final var dependency = new DependencyConfig("zerolog", "com.obsidiandynamics.zerolog", "zerolog-core", null);
    final var version = new MavenRepository().resolveLatestVersion(getContext(), dependency);
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
  
  @Test(expected=MavenResponseException.class)
  public void testNonexistentPackage() throws Exception {
    final var dependency = new DependencyConfig("zerolog", "com.obsidiandynamics.zerolog", "foo", null);
    new MavenRepository().resolveLatestVersion(getContext(), dependency);
  }
}
