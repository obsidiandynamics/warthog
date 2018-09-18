package com.obsidiandynamics.warthog;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

public final class GradleTransform {
  private GradleTransform() {}
  
  /**
   *  Matches strings in the form '  nameVersion = "x.y.z" // trailing comment', the spaces around the '='
   *  character and the text before and after the statement being optional. The 'name' portion is the
   *  {@code dependencyName} argument.
   *  
   *  @param dependencyName The dependency name.
   *  @return The {@link Pattern} instance.
   */
  private static Pattern getDependencyVersionPattern(String dependencyName) {
    return Pattern.compile("^(.*" + dependencyName + "Version\\s*=\\s*[\"'])(.*)([\"'].*)$");
  }
  
  public static List<Update> updateDependencies(File buildFile, Map<String, String> namesToVersions) throws IOException {
    final var tempFile = new File(buildFile + ".tmp");
    tempFile.deleteOnExit();
    if (tempFile.exists()) tempFile.delete();
    
    final var patterns = buildPatterns(namesToVersions.keySet());
    final var updates = new ArrayList<Update>();
    try (var reader = new BufferedReader(new FileReader(buildFile));
         var writer = new BufferedWriter(new FileWriter(tempFile))) {
      for (var line = reader.readLine(); line != null; line = reader.readLine()) {
        final var update = updateSingleLine(line, namesToVersions, patterns);
        writer.write(update.getLine());
        writer.newLine();
        if (update.isChanged()) {
          updates.add(update);
        }
      }
    }
    
    tempFile.renameTo(buildFile);
    return updates;
  }
  
  public static final class Update {
    private final String line;
    private final String dependencyName;
    private final String oldVersion;
    private final String newVersion;
    
    Update(String line, String dependencyName, String oldVersion, String newVersion) {
      this.line = line;
      this.dependencyName = dependencyName;
      this.oldVersion = oldVersion;
      this.newVersion = newVersion;
    }
    
    String getLine() {
      return line;
    }

    public String getDependencyName() {
      return dependencyName;
    }

    public String getOldVersion() {
      return oldVersion;
    }

    public String getNewVersion() {
      return newVersion;
    }
    
    public boolean isChanged() {
      return dependencyName != null;
    }
  }
  
  static Map<String, Pattern> buildPatterns(Collection<String> dependencyNames) {
    return dependencyNames.stream().collect(Collectors.toMap(Function.identity(), GradleTransform::getDependencyVersionPattern));
  }
  
  static Update updateSingleLine(String line, Map<String, String> namesToVersions, Map<String, Pattern> patterns) {
    for (var entry : namesToVersions.entrySet()) {
      final var dependencyName = entry.getKey();
      // we're looking for a line in the form of (ignore single quotes):
      // '  declaration nameVersion = "x.y.z" // followed by an optional comment'
      final var matcher = patterns.get(dependencyName).matcher(line);
      if (matcher.matches()) {
        final var leadingText = matcher.group(1);   // '  nameVersion = "'
        final var versionText = matcher.group(2);   // 'x.y.z'
        final var trailingText = matcher.group(3);  // '" // trailing comment'
        final var newVersion = entry.getValue();
        if (! versionText.equals(newVersion)) {
          // the version has changed — reconstruct the line with the new version
          final var updatedLine = leadingText + newVersion + trailingText;
          return new Update(updatedLine, dependencyName, versionText, newVersion);
        } else {
          return new Update(line, null, null, null); // the versions are the same — return unchanged
        }
      }
    }
    
    return new Update(line, null, null, null); // scanned through all versions and found nothing — returned unchanged
  }
}
