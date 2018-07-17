package com.obsidiandynamics.warthog;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class WarthogBannerTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(WarthogBanner.class);
  }
  
  @Test
  public void testPrint() {
    final var out = mock(PrintStream.class);
    WarthogBanner.print(out);
    verify(out, atLeastOnce()).println(any(String.class));
  }
}
