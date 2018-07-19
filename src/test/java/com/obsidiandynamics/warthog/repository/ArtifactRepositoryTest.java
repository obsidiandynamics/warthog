package com.obsidiandynamics.warthog.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.warthog.config.*;

public final class ArtifactRepositoryTest {
  @Test
  public void testBulkResolve() throws Exception {
    final var repo = mock(ArtifactRepository.class, Answers.CALLS_REAL_METHODS);
    when(repo.resolveLatestVersion(any(), any()))
    .thenAnswer(invocation -> {
      final var dependency = invocation.<DependencyConfig>getArgument(1);
      return dependency.getGroupId() + ".0";
    });
    
    final var namesToVersions = repo
        .bulkResolve(null, 
                     new DependencyConfig[] {
                                             new DependencyConfig("fulcrum", "fulcrumGroup", "fulcrumArtifact", null),
                                             new DependencyConfig("yconf", "yconfGroup", "yconfArtifact", null)});
    assertEquals(2, namesToVersions.size());
    assertEquals("fulcrumGroup.0", namesToVersions.get("fulcrum"));
    assertEquals("yconfGroup.0", namesToVersions.get("yconf"));
  }
}
