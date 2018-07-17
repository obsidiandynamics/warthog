package com.obsidiandynamics.warthog;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.version.*;

public final class WarthogVersion {
  private WarthogVersion() {}
  
  public static String get() {
    return Exceptions.wrap(() -> AppVersion.get("warthog"), RuntimeException::new);
  }
}
