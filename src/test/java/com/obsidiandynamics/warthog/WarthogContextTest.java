package com.obsidiandynamics.warthog;

import static org.junit.Assert.*;

import java.io.*;

import org.apache.http.impl.nio.client.*;
import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.warthog.args.*;
import com.obsidiandynamics.warthog.config.*;

public final class WarthogContextTest {
  @Test
  public void testPojo() {
    // can't use PojoVerifier here because of PrintStream — the fabricated instance tries to write to a file
    final var out = Mockito.mock(PrintStream.class);
    final var args = Args.parse();
    final var project = new ProjectConfig(null, null);
    final var httpClient = (CloseableHttpAsyncClient) null;
    final var context = new WarthogContext(out, args, project, httpClient);
    assertEquals(out, context.getOut());
    assertEquals(args, context.getArgs());
    assertEquals(project, context.getProject());
    assertEquals(httpClient, context.getHttpClient());
    Assertions.assertToStringOverride(context);
  }
}
