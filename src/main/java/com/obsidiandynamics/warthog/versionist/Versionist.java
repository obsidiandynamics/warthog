package com.obsidiandynamics.warthog.versionist;

import java.io.*;

public interface Versionist {
  String getVersion(String projectDirectory) throws IOException;
  
  void setVersion(String projectDirectory, String newVersion) throws IOException;
  
  String describe();
}
