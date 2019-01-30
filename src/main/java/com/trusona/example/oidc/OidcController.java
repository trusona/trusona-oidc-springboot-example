package com.trusona.example.oidc;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 * Copyright Trusona, Inc.
 */
@Controller
@RequestMapping("/")
public class OidcController {

  private static final String OIDC_RESPONSE = "/oidc_response";
  private static final String OIDC_TEMPLATE = "oidc";
  private static final String SUCCESS_TEMPLATE = "success";

  private static final String AUTHORIZATION_ENDPOINT = "authorization_endpoint";
  private static final String JWKS_URI = "jwks_uri";

  private final OidcConfig oidcConfig;
  private String authorizationEndpoint;
  private String jwksUri;

  @Autowired
  public OidcController(OidcConfig oidcConfig) {
    this.oidcConfig = oidcConfig;
  }

  @PostConstruct
  void initializeOidcConfiguration() {
    RestTemplate restTemplate = new RestTemplate();
    Map oidcDiscoveryConfig = restTemplate.getForObject(oidcConfig.getDiscoveryUrl(), Map.class);

    if (oidcDiscoveryConfig == null) {
      throw new IllegalStateException("missing discovery-url configuration");
    }

    this.authorizationEndpoint = (String) oidcDiscoveryConfig.get(AUTHORIZATION_ENDPOINT);
    this.jwksUri = (String) oidcDiscoveryConfig.get(JWKS_URI);
  }

  @RequestMapping(method = GET)
  String handleLoginWithOidc(Map<String, Object> model) {
    model.put("authorizationEndpoint", authorizationEndpoint);
    model.put("nonce", UUID.randomUUID());
    model.put("state", UUID.randomUUID());
    model.put("clientId", oidcConfig.getClientId());
    model.put("redirectUri", oidcConfig.getRedirectUri());
    return OIDC_TEMPLATE;
  }

  @RequestMapping(path = OIDC_RESPONSE, method = GET)
  String handleErrorWithOidc(Map<String, Object> model) {
    model.put("authorizationEndpoint", authorizationEndpoint);
    model.put("error", true);
    model.put("nonce", UUID.randomUUID());
    model.put("state", UUID.randomUUID());
    model.put("clientId", oidcConfig.getClientId());
    model.put("redirectUri", oidcConfig.getRedirectUri());
    return OIDC_TEMPLATE;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(path = OIDC_RESPONSE, method = POST)
  String handleOidcResponse(@RequestParam("id_token") String idToken, Map<String, Object> model)
    throws ParseException, IOException, BadJOSEException, JOSEException {

    SignedJWT token = SignedJWT.parse(idToken);

    JWSAlgorithm expectedAlgorithm = JWSAlgorithm.RS256;
    JWKSource keySource = new RemoteJWKSet(new URL(jwksUri));
    JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedAlgorithm, keySource);

    ConfigurableJWTProcessor processor = new DefaultJWTProcessor();
    processor.setJWSKeySelector(keySelector);

    JWTClaimsSet claimsSet = processor.process(token, null);

    // You should verify the token here

    model.put("claims", claimsSet.getClaims().entrySet());
    return SUCCESS_TEMPLATE;
  }
}