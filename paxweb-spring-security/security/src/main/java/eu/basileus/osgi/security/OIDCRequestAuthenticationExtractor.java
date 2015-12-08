package eu.basileus.osgi.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.ServletUtils;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OIDCRequestAuthenticationExtractor {

	JwtVerifier accessTokenVerifier;
	JwtVerifier idTokenVerifier;
	ServerConfigurationService serverConfigurationService;
	
	private BearerAccessToken parseAccessToken(HttpServletRequest request) throws IOException {
		HTTPRequest httpRequest = ServletUtils.createHTTPRequest(request);
		try {
			BearerAccessToken accessToken = BearerAccessToken.parse(httpRequest);
			JWT accessTokenJwt = JWTParser.parse(accessToken.getValue());
			String error = accessTokenVerifier.verify(accessTokenJwt);
			if (error != null) {
				throw new AuthenticationServiceException(error);
			}
			return accessToken;
		} catch (ParseException | java.text.ParseException e) {
			throw new AuthenticationServiceException("Could not parse access token", e);
		}
	}
	
	private JWT parseIdToken(HttpServletRequest request) {
		String idToken = request.getHeader("IdToken");
		if (idToken == null) {
			throw new AuthenticationServiceException("IdToken header not found");
		}
		try {
			JWT idTokenJwt = JWTParser.parse(idToken);
			String error = idTokenVerifier.verify(idTokenJwt);
			if (error != null) {
				throw new AuthenticationServiceException(error);
			}
			return idTokenJwt;
 		} catch (java.text.ParseException e) {
			throw new AuthenticationServiceException("Could not parse idToken", e);
		}
	}

	public PendingOIDCAuthenticationToken parse(HttpServletRequest request) throws IOException {
		BearerAccessToken accessToken = parseAccessToken(request);
		JWT idToken = parseIdToken(request);
		try {
		JWTClaimsSet idTokenClaimsSet = idToken.getJWTClaimsSet();
		return new PendingOIDCAuthenticationToken(idTokenClaimsSet.getSubject(), 
										   		  idTokenClaimsSet.getIssuer(), 
										   		  serverConfigurationService.getServerConfiguration(idTokenClaimsSet.getIssuer()), 
										          idToken, 
										          accessToken.getValue(), 
										          null);
		} catch (java.text.ParseException e) {
			throw new AuthenticationServiceException("Could not parse idToken", e);
		}
	}

}
