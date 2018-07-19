package com.obsidiandynamics.warthog;

import static org.junit.Assert.*;

import java.io.*;
import java.security.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class HttpClientTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(HttpClient.class);
  }
  
  @Test
  public void testCreate() throws NoSuchAlgorithmException, IOException {
    final var client = HttpClient.create();
    assertNotNull(client);
    client.close();
  }
}
