package com.obsidiandynamics.warthog;

import java.io.*;

import org.apache.http.impl.nio.client.*;

import com.obsidiandynamics.warthog.args.*;
import com.obsidiandynamics.warthog.config.*;

public final class WarthogContext {
  private final PrintStream out;
  
  private final Args args;
  
  private final ProjectConfig project;
  
  private final CloseableHttpAsyncClient httpClient;

  public WarthogContext(PrintStream out, Args args, ProjectConfig project, CloseableHttpAsyncClient httpClient) {
    this.out = out;
    this.args = args;
    this.project = project;
    this.httpClient = httpClient;
  }

  public PrintStream getOut() {
    return out;
  }

  public Args getArgs() {
    return args;
  }

  public ProjectConfig getProject() {
    return project;
  }

  public CloseableHttpAsyncClient getHttpClient() {
    return httpClient;
  }
}
