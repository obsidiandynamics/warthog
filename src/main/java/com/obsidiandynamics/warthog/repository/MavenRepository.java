package com.obsidiandynamics.warthog.repository;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

import com.obsidiandynamics.func.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.util.*;

import com.fasterxml.jackson.dataformat.xml.*;
import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.config.*;

/**
 *  Provides a reliable mechanism for querying Maven-compliant repositories and mimics 
 *  Gradle/Maven's behaviour for dependency resolution.
 */
public final class MavenRepository implements ArtifactRepository {
  private final XmlMapper mapper = new XmlMapper();
  
  static final class MavenResponseException extends Exception {
    private static final long serialVersionUID = 1L;
    
    MavenResponseException(String m) { super(m); }
  }

  @SuppressWarnings("unchecked")
  private static <T> T cast(Object obj) {
    return (T) obj;
  }
  
  @Override
  public String resolveLatestVersion(WarthogContext context, DependencyConfig dependency) throws Exception {
    final var groupIdUrlFrag = dependency.getGroupId().replace('.', '/');
    final var repoUrl = dependency.getRepoUrl();
    final var url = repoUrl + "/" + groupIdUrlFrag + "/" + dependency.getArtifactId() + "/maven-metadata.xml";
    final var get = new HttpGet(url);
    get.setHeader("Accepts", ContentType.APPLICATION_XML.getMimeType());
    final var response = context.getHttpClient().execute(get, null).get();
    mustBeEqual(HttpStatus.SC_OK, response.getStatusLine().getStatusCode(),
                withMessage("Call to " + url + " resulted in " + response.getStatusLine(), MavenResponseException::new));
    
    final var responseXml = EntityUtils.toString(response.getEntity());
    final var responseMap = mapper.readValue(responseXml, Map.class);
    final Map<Object, Object> versioningElement = Functions.mustExist(cast(responseMap.get("versioning")), withMessage("Missing XML element <versioning>", MavenResponseException::new));
    return Functions.mustExist(cast(versioningElement.get("release")), withMessage("Missing XML element <release>", MavenResponseException::new));
  }
}
