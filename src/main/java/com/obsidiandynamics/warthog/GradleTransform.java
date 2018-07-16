package com.obsidiandynamics.warthog;

import java.io.*;
import java.util.*;

public final class GradleTransform {
  private GradleTransform() {}
  
  public static List<Update> updateDependencies(File buildFile, Map<String, String> namesToVersions) throws FileNotFoundException, IOException {
    final var tempFile = new File(buildFile + ".tmp");
    tempFile.deleteOnExit();
    if (tempFile.exists()) tempFile.delete();
    
    final var updates = new ArrayList<Update>();
    var firstLine = true;
    try (var reader = new BufferedReader(new FileReader(buildFile));
         var writer = new BufferedWriter(new FileWriter(tempFile))) {
      for (var line = reader.readLine(); line != null; line = reader.readLine()) {
        if (firstLine) {
          firstLine = false;
        } else {
          writer.newLine();
        }
        
        final var update = updateSingleLine(line, namesToVersions);
        writer.write(update.getLine());
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
    
    boolean isChanged() {
      return dependencyName != null;
    }
  }
  
  static Update updateSingleLine(String line, Map<String, String> namesToVersions) {
    for (var entry : namesToVersions.entrySet()) {
      final var dependencyName = entry.getKey();
      // we're looking for a line in the form of (ignore single quotes):
      // 'xxxVersion = "x.y.z" // followed by an optional comment'
      final var variableNameAndAssignmentOp = dependencyName + "Version = ";
      final var indexOfVariable = line.indexOf(variableNameAndAssignmentOp);
      if (indexOfVariable != -1) {
        // extract the 'xxxVersion = ' bit and any leading indentation
        final var leadingText = line.substring(0, indexOfVariable + variableNameAndAssignmentOp.length());
        
        // preserve everything after the closing quote — the trailing spaces and the inline comment
        final var indexOfClosingQuote = line.indexOf('"', indexOfVariable + variableNameAndAssignmentOp.length() + 1);
        final var trailingText = line.substring(indexOfClosingQuote + 1);
        final var oldVersion = line.substring(indexOfVariable + variableNameAndAssignmentOp.length() + 1, indexOfClosingQuote);
        final var newVersion = entry.getValue();
        if (! oldVersion.equals(newVersion)) {
          // the version has changed — reconstruct the line with the new version
          final var updatedLine = leadingText + "\"" + entry.getValue() + "\"" + trailingText;
          return new Update(updatedLine, dependencyName, oldVersion, newVersion);
        } else {
          return new Update(line, null, null, null); // the versions are the same — return unchanged
        }
      }
    }
    
    return new Update(line, null, null, null); // scanned through all versions and found nothing — returned unchanged
  }
}
