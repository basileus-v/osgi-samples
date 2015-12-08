package eu.basileus.osgi.security;

import java.util.Arrays;

import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * An example on how to build {@link OIDCAuthenticationFilter} (without any DI framework).
 * 
 * @author Aleksei Lissitsin
 */
public class OIDCAuthenticationFilterFactory {
	public static OIDCAuthenticationFilter create(){
		ServerConfigurationService serverConfigService = new DynamicServerConfigurationService();
		JWKSetCacheService jwkSetCacheService = new JWKSetCacheService();
		IdTokenValidator validator = new IdTokenValidator(serverConfigService, jwkSetCacheService::getValidator , 1);
		OIDCRequestAuthenticationExtractor authenticationExtractor = new OIDCRequestAuthenticationExtractor();
		authenticationExtractor.setAccessTokenVerifier(validator::validate);
		authenticationExtractor.setIdTokenVerifier(validator::validate);
		authenticationExtractor.setServerConfigurationService(serverConfigService);
		OIDCAuthenticationFilter authenticationFilter = new OIDCAuthenticationFilter();
		authenticationFilter.setAuthenticationExtractor(authenticationExtractor);
		authenticationFilter.setAuthorityGranter((s) -> Arrays.asList(new SimpleGrantedAuthority("ROLE_API")));
		return authenticationFilter;
	}
}
