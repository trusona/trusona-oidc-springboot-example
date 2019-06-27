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
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(OidcController.class);
  private static final JWSAlgorithm JWS_ALGORITHM = JWSAlgorithm.RS256;
  private static final String OIDC_RESPONSE = "/oidc_response";
  private static final String SUCCESS_TEMPLATE = "success";
  private static final String OIDC_TEMPLATE = "oidc";

  private JWKSource<SecurityContext> keySource;
  private final OidcConfig oidcConfig;
  private URL authorizationEndpoint;

  @Autowired
  public OidcController(OidcConfig oidcConfig) {
    this.oidcConfig = oidcConfig;
  }

  @PostConstruct
  void initializeOidcConfiguration() throws MalformedURLException {
    Map oidcDiscoveryConfig = Optional.ofNullable(new RestTemplate().getForObject(oidcConfig.getDiscoveryUrl(), Map.class))
      .orElseThrow(() -> new IllegalStateException("missing discovery-url configuration"));

    this.authorizationEndpoint = new URL((String) oidcDiscoveryConfig.get("authorization_endpoint"));
    this.keySource = new RemoteJWKSet<>(new URL((String) oidcDiscoveryConfig.get("jwks_uri")));

    logger.info("Loaded discovery data from {}", oidcConfig.getDiscoveryUrl());
  }

  @RequestMapping(method = GET)
  String handleLoginWithOidc(Map<String, Object> model) {
    model.put("authorizationEndpoint", authorizationEndpoint);
    model.put("redirectUri", oidcConfig.getRedirectUri());
    model.put("clientId", oidcConfig.getClientId());
    model.put("nonce", UUID.randomUUID());
    model.put("state", UUID.randomUUID());

    return OIDC_TEMPLATE;
  }

  @RequestMapping(path = OIDC_RESPONSE, method = GET)
  String handleErrorWithOidc(Map<String, Object> model) {
    model.put("authorizationEndpoint", authorizationEndpoint);
    model.put("redirectUri", oidcConfig.getRedirectUri());
    model.put("clientId", oidcConfig.getClientId());
    model.put("nonce", UUID.randomUUID());
    model.put("state", UUID.randomUUID());
    model.put("error", true);

    return OIDC_TEMPLATE;
  }

  @RequestMapping(path = OIDC_RESPONSE, method = POST)
  String handleOidcResponse(@RequestParam("id_token") String idToken, Map<String, Object> model)
    throws ParseException, BadJOSEException, JOSEException {

    logger.info("id-token => {}", idToken);

    SignedJWT signedJWT = SignedJWT.parse(idToken);

    JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWS_ALGORITHM, keySource);
    ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

    processor.setJWSKeySelector(keySelector);

    JWTClaimsSet claimsSet = processor.process(signedJWT, null);

    // verify the expected subject and or claims from the JWT for your purposes
    //
    // if the JWT is invalid or expired, one of the concrete exceptions will
    // be thrown and we shall not get here

    model.put("claims", claimsSet.getClaims().entrySet());
    return SUCCESS_TEMPLATE;
  }
}