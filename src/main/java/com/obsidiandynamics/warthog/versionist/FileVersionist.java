package com.obsidiandynamics.warthog.versionist;

import java.io.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class FileVersionist implements Versionist {
  private final String versionFile;
  
  public FileVersionist(@YInject(name="versionFile") String versionFile) {
    this.versionFile = versionFile;
  }
  
  public String getVersionFile() {
    return versionFile;
  }
  
  @Override
  public String getVersion(String projectDirectory) throws IOException {
    final var versionFile = getVersionFile(projectDirectory);
    if (versionFile.exists()) {
      try (var reader = new BufferedReader(new FileReader(versionFile))) {
        final var version = reader.readLine().trim();
        return ! version.isEmpty() ? version : null;
      }
    } else {
      return null;
    }
  }

  @Override
  public void setVersion(String projectDirectory, String newVersion) throws IOException {
    final var versionFile = getVersionFile(projectDirectory);
    try (var writer = new BufferedWriter(new FileWriter(versionFile, false))) {
      writer.write(newVersion);
      writer.write('\n');
    }
  }
  
  @Override
  public String describe() {
    return "File versionist using version file " + versionFile;
  }
  
  private File getVersionFile(String projectDirectory) {
    return new File(projectDirectory + "/" + versionFile);
  }
}
