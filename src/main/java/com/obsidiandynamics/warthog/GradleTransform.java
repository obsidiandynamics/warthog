package com.obsidiandynamics.warthog;

import java.io.*;
import java.util.*;

public final class GradleTransform {
  private GradleTransform() {}
  
  public static void updateDependencies(File buildFile, Map<String, String> namesToVersions) throws FileNotFoundException, IOException {
    final var tempFile = new File(buildFile + ".tmp");
    tempFile.deleteOnExit();
    if (tempFile.exists()) tempFile.delete();
    
    try (var reader = new BufferedReader(new FileReader(buildFile));
         var writer = new BufferedWriter(new FileWriter(tempFile))) {
      for (var line = reader.readLine(); line != null; line = reader.readLine()) {
        final var updatedLine = updateSingleLine(line, namesToVersions);
        writer.write(updatedLine.line);
        writer.newLine();
      }
    }
  }
  
  static final class Update {
    final String line;
    final String dependencyName;
    final String oldVersion;
    final String newVersion;
    
    public Update(String line, String dependencyName, String oldVersion, String newVersion) {
      this.line = line;
      this.dependencyName = dependencyName;
      this.oldVersion = oldVersion;
      this.newVersion = newVersion;
    }
  }
  
  static Update updateSingleLine(String line, Map<String, String> namesToVersions) {
    for (var entry : namesToVersions.entrySet()) {
      // we're looking for a line in the form of (ignore single quotes):
      // 'xxxVersion = "x.y.z" // followed by an optional comment'
      final var variableNameAndAssignmentOp = entry.getKey() + "Version = ";
      final var indexOfVariable = line.indexOf(variableNameAndAssignmentOp);
      if (indexOfVariable != -1) {
        // extract the 'xxxVersion = ' bit and any leading indentation
        final var leadingText = line.substring(0, indexOfVariable + variableNameAndAssignmentOp.length());
        
        // preserve everything after the closing quote — the trailing spaces and the inline comment
        final var indexOfClosingQuote = line.indexOf('"', indexOfVariable + variableNameAndAssignmentOp.length() + 1);
        final var trailingText = line.substring(indexOfClosingQuote + 1);
        final var oldVersion = line.substring(indexOfVariable + variableNameAndAssignmentOp.length() + 1, indexOfClosingQuote);
        
        // reconstruct the line with the new version
        final var updatedLine = leadingText + "\"" + entry.getValue() + "\"" + trailingText;
        return new Update(updatedLine, entry.getKey(), oldVersion, entry.getValue());
      }
    }
    
    return new Update(line, null, null, null); // scanned through all versions and found nothing — returned unchanged
  }
}
