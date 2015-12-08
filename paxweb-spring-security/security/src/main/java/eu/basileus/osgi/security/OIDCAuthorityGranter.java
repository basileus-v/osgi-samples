package eu.basileus.osgi.security;

import java.util.List;

import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public interface OIDCAuthorityGranter {
	List<GrantedAuthority> grant(PendingOIDCAuthenticationToken token);
}
