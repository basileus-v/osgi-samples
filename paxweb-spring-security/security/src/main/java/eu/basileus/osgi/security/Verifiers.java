package eu.basileus.osgi.security;

import java.util.Date;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;

import com.nimbusds.jwt.JWTClaimsSet;

public class Verifiers {
	public static Verifier issuer(ServerConfiguration serverConfiguration) {
		return claims -> issuer(serverConfiguration, claims);
	}
	
	public static Verifier expirationTime(int timeSkewAllowance) {
		return claims -> expirationTime(timeSkewAllowance, claims);
	}
	
	public static Verifier notBeforeTime(int timeSkewAllowance) {
		return claims -> notBeforeTime(timeSkewAllowance, claims);
	}
	
	public static Verifier issueTime(int timeSkewAllowance) {
		return claims -> issueTime(timeSkewAllowance, claims);
	}
	
	public static Verifier audience(RegisteredClient client) {
		return claims -> audience(client, claims);
	}
	
	public static String issuer(ServerConfiguration serverConfig, JWTClaimsSet claims) {
		if (claims.getIssuer() == null) {
			return "Issuer is null";
		}
		if (!claims.getIssuer().equals(serverConfig.getIssuer())) {
			return "Issuers do not match, expected " + serverConfig.getIssuer() + " got " + claims.getIssuer();
		}
		return null;
	}

	public static String expirationTime(int timeSkewAllowance, JWTClaimsSet claims) {
		if (claims.getExpirationTime() == null) {
			return "No required expiration time";
		}

		Date now = new Date(System.currentTimeMillis() - (timeSkewAllowance * 1000));
		if (now.after(claims.getExpirationTime())) {
			return "Expired: " + claims.getExpirationTime();
		}
		return null;
	}
	

	public static String notBeforeTime(int timeSkewAllowance, JWTClaimsSet claims) {
		if (claims.getNotBeforeTime() != null) {
			Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
			if (now.before(claims.getNotBeforeTime())) {
				return "Not valid until: " + claims.getNotBeforeTime();
			}
		}
		return null;
	}

	public static String issueTime(int timeSkewAllowance, JWTClaimsSet claims) {
		if (claims.getIssueTime() == null) {
			return "No required issued-at claim";
		}

		Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
		if (now.before(claims.getIssueTime())) {
			return "Issued in the future: " + claims.getIssueTime();
		}
		return null;
	}

	public static String audience(RegisteredClient client, JWTClaimsSet claims) {
		if (claims.getAudience() == null) {
			return "Audience is null";
		}

		if (!claims.getAudience().contains(client.getClientId())) {
			return "Audience does not match, expected " + client.getClientId() + " got " + claims.getAudience();
		}
		return null;
	}

}
