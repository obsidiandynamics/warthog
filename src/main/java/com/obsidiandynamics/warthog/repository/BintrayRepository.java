package com.obsidiandynamics.warthog.repository;

import java.util.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.util.*;

import com.fasterxml.jackson.databind.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.warthog.*;
import com.obsidiandynamics.warthog.config.*;

/**
 *  Uses the Maven query capability of Bintray to resolve the package version.<p>
 *  
 *  This has been superseded by {@link MavenRepository}, which provides a reliable
 *  mechanism for querying Maven repositories and will mimic Gradle's behaviour.
 */
@Deprecated
public final class BintrayRepository implements ArtifactRepository {
  private final ObjectMapper mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
  
  static final class BintrayResponseException extends Exception {
    private static final long serialVersionUID = 1L;
    
    BintrayResponseException(String m) { super(m); }
  }
  
  @Override
  public String resolveLatestVersion(WarthogContext context, DependencyConfig dependency) throws Exception {
    final var url = String.format("https://bintray.com/api/v1/search/packages/maven?g=%s&a=%s", 
                                  dependency.getGroupId(), dependency.getArtifactId());
    final var get = new HttpGet(url);
    get.setHeader("Accepts", ContentType.APPLICATION_JSON.getMimeType());
    final var response = context.getHttpClient().execute(get, null).get();
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new BintrayResponseException("Call to " + url + " resulted in " + response.getStatusLine());
    }
    
    final var responseJson = EntityUtils.toString(response.getEntity());
    final var responseList = mapper.readValue(responseJson, List.class);
    if (responseList.size() != 1) {
      throw new BintrayResponseException("Expected singleton array, got " + responseList);
    }
    
    final var packageInfo = Classes.<Map<String, Object>>cast(responseList.get(0));
    final var latestVersion = (String) packageInfo.get("latest_version");
    if (latestVersion == null) {
      throw new BintrayResponseException("Missing attribute 'latest_version'");
    }
    
    return latestVersion;
  }
}
