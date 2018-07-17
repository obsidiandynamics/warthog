package com.obsidiandynamics.warthog.repository;

import static org.junit.Assert.*;

import java.io.*;
import java.security.*;

import org.apache.http.impl.nio.client.*;
import org.apache.http.nio.reactor.*;
import org.fusesource.jansi.*;
import org.junit.*;

import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.repository.BintrayRepository.*;

public final class BintrayRepositoryIT {
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
    final var version = new BintrayRepository().resolveLatestVersion(getContext(), "com.obsidiandynamics.zerolog", "zerolog-core");
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
  
  @Test(expected=BintrayResponseException.class)
  public void testNonExistentPackage() throws Exception {
    final var version = new BintrayRepository().resolveLatestVersion(getContext(), "com.obsidiandynamics.zerolog", "foo");
    assertTrue("version=" + version, version.matches("^\\d+\\.\\d+\\.\\d+$"));
  }
}
