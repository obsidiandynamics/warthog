package com.obsidiandynamics.warthog.config;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class ProjectConfigTest {
  @Test
  public void testFromUri() throws FileNotFoundException, IOException {
    final var project = ProjectConfig.fromUri(URI.create("cp://test.hog.project"));
    assertNotNull(project);
    assertEquals("./gradlew test cleanIntegrationTest integrationTest --info --stacktrace --no-daemon", project.getCommands().getBuild());
    assertEquals("./gradlew -x test bintrayUpload --no-daemon", project.getCommands().getPublish());
    
    assertEquals(3, project.getModules().length);
    
    assertEquals(".", project.getModules()[0].getPath());
    assertEquals(3, project.getModules()[0].getDependencies().length);
    assertEquals("fulcrum", project.getModules()[0].getDependencies()[0].getName());
    assertEquals("com.obsidiandynamics.fulcrum", project.getModules()[0].getDependencies()[0].getGroupId());
    assertEquals("fulcrum-func", project.getModules()[0].getDependencies()[0].getArtifactId());
    assertEquals("http://jcenter.bintray.com", project.getModules()[0].getDependencies()[0].getRepoUrl());

    assertEquals("ledger-meteor", project.getModules()[1].getPath());
    assertEquals(1, project.getModules()[1].getDependencies().length);
    assertEquals("meteor", project.getModules()[1].getDependencies()[0].getName());
    assertEquals("com.obsidiandynamics.meteor", project.getModules()[1].getDependencies()[0].getGroupId());
    assertEquals("meteor-core", project.getModules()[1].getDependencies()[0].getArtifactId());
    assertEquals("http://repo1.maven.org/maven2", project.getModules()[1].getDependencies()[0].getRepoUrl());

    assertEquals("ledger-kafka", project.getModules()[2].getPath());
    assertEquals(1, project.getModules()[2].getDependencies().length);
    assertEquals("jackdaw", project.getModules()[2].getDependencies()[0].getName());
    assertEquals("com.obsidiandynamics.jackdaw", project.getModules()[2].getDependencies()[0].getGroupId());
    assertEquals("jackdaw-core", project.getModules()[2].getDependencies()[0].getArtifactId());
    assertEquals("http://jcenter.bintray.com", project.getModules()[2].getDependencies()[0].getRepoUrl());
  }
  
  @Test
  public void testPojo() {
    PojoVerifier.forClass(ProjectConfig.class).excludeToStringField("modules").verify();
  }
}
