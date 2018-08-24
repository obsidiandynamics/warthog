package com.obsidiandynamics.warthog.versionist;

import java.io.*;
import java.util.regex.*;

import com.obsidiandynamics.yconf.*;

@Y
public final class GradleVersionist implements Versionist {
  @Override
  public String getVersion(String projectDirectory) throws IOException {
    final var buildFile = getRootBuildFile(projectDirectory);
    final var pattern = getProjectVersionPattern();
    try (var reader = new BufferedReader(new FileReader(buildFile))) {
      for (var line = reader.readLine(); line != null; line = reader.readLine()) {
        final var matcher = pattern.matcher(line);
        if (matcher.matches()) {
          return matcher.group(2);
        }
      }
    }
    return null;
  }

  @Override
  public void setVersion(String projectDirectory, String newVersion) throws IOException {
    final var buildFile = getRootBuildFile(projectDirectory);
    final var tempFile = new File(buildFile + ".tmp");
    tempFile.deleteOnExit();
    if (tempFile.exists()) tempFile.delete();
    
    final var pattern = getProjectVersionPattern();
    try (var reader = new BufferedReader(new FileReader(buildFile));
         var writer = new BufferedWriter(new FileWriter(tempFile))) {
      for (var line = reader.readLine(); line != null; line = reader.readLine()) {
        final var matcher = pattern.matcher(line);
        if (matcher.matches()) {
          final var leadingText = matcher.group(1);   // '  version = "'
          final var trailingText = matcher.group(3);  // '" // trailing comment'
          final var updatedLine = leadingText + newVersion + trailingText;
          writer.write(updatedLine);
        } else {
          writer.write(line);
        }
        writer.newLine();
      }
    }
    
    tempFile.renameTo(buildFile);
  }
  
  @Override
  public String describe() {
    return "Gradle versionist";
  }
  
  private static File getRootBuildFile(String projectDirectory) {
    return new File(projectDirectory + "/build.gradle");
  }
  
  /**
   *  Matches strings in the form '  version = "x.y.z" // trailing comment', the spaces around the '='
   *  character and the text before and after the statement being optional.
   *  
   *  @return The {@link Pattern} instance.
   */
  private static Pattern getProjectVersionPattern() {
    return Pattern.compile("^(\\s*version\\s*=\\s*\")(.*)(\".*)$");
  }
}
