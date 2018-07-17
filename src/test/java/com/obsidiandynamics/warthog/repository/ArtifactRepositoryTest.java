package com.obsidiandynamics.warthog.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.warthog.config.*;
import com.obsidiandynamics.warthog.repository.*;

public final class ArtifactRepositoryTest {
  @Test
  public void testBulkResolve() throws Exception {
    final var lookup = mock(ArtifactRepository.class, Answers.CALLS_REAL_METHODS);
    when(lookup.resolveLatestVersion(any(String.class), any(String.class))).thenAnswer(invocation -> {
      final var groupId = invocation.<String>getArgument(0);
      return groupId + ".0";
    });
    
    final var namesToVersions = lookup
        .bulkResolve(new DependencyConfig[] {
                                             new DependencyConfig("fulcrum", "fulcrumGroup", "fulcrumArtifact"),
                                             new DependencyConfig("yconf", "yconfGroup", "yconfArtifact")});
    assertEquals(2, namesToVersions.size());
    assertEquals("fulcrumGroup.0", namesToVersions.get("fulcrum"));
    assertEquals("yconfGroup.0", namesToVersions.get("yconf"));
  }
}
