package dev.pedrobittencourt.bittencourt_academy.auth.oauth2;

import dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider.AuthenticationProvider;
import dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider.AuthenticationProviderRepository;
import dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider.AuthenticationProviderService;
import dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider.AuthenticationProviderType;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import dev.pedrobittencourt.bittencourt_academy.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService extends OidcUserService { // Posteriormente fazer teste de INTEGRAÇÃO
    private final UserService userService;
    private final AuthenticationProviderService authenticationProviderService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest){
        OidcUser oidcUser = super.loadUser(userRequest);

        String sub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        AuthenticationProvider provider = authenticationProviderService.findGoogleProvider(sub).orElse(null);

        // Cadastro o usuário caso ele não esteja cadastrado
        if(provider == null){
            User savedUser = userService.saveGoogle(email, name);
            authenticationProviderService.createGoogleProvider(savedUser, sub);
        }

        return oidcUser;
    }
}
