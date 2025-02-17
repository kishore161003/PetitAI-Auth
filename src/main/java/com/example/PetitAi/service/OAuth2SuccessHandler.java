package com.example.PetitAi.service;

import com.example.PetitAi.entity.Users;
import com.example.PetitAi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

//http://localhost:8080/oauth2/authorization/google
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JWTService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub"); // Google unique ID
        String profilePic = oAuth2User.getAttribute("picture");

        // Check if user exists in MongoDB
        Optional<Users> existingUser = userRepository.findByEmail(email);

        Users user;
        if (existingUser.isEmpty()) {
            // Create a new user if not exists
            user = new Users();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setProfilePic(profilePic);
            user.setActive(true);

            user = userRepository.save(user); // Save to MongoDB
        } else {
            user = existingUser.get();
        }

        // Generate JWT Token
        String token = jwtService.generateToken(user.getEmail());

        // Redirect user to frontend with token
        response.sendRedirect("http://localhost:3000/login-success?token=" + token);
    }
}