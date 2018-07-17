package com.obsidiandynamics.warthog.args;

import java.util.function.*;

import com.beust.jcommander.*;
import com.obsidiandynamics.props.*;

public final class CommonArgs {
  @Parameter(names={"-d", "--directory"}, description="Sets the project directory")
  private String directory = Props.get("warthog.dir", Function.identity(), ".");
  
  @Parameter(names="--help", help=true, description="Displays help")
  private boolean help;
  
  public String getDirectory() {
    return directory;
  }
  
  public boolean isHelp() {
    return help;
  }
}