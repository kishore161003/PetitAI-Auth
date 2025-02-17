package com.example.PetitAi.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.example.PetitAi.entity.Users;
import com.example.PetitAi.entity.VerificationToken;
import com.example.PetitAi.repository.UserRepository;
import com.example.PetitAi.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;


    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    private EmailService emailService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Value("${LINK}") // Reads from application.properties or environment
    private String link;

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Users> getUsersByGender(String gender) {
        return userRepository.findByGender(gender);
    }

    public Optional<Users> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public ResponseEntity<String> registerUser(Users user) {
        Optional<Users> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered!");
        }

        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserName(user.getName());
        verificationToken.setHashedPassword(bCryptPasswordEncoder.encode(user.getHashedPassword()));
        verificationToken.setEmail(user.getEmail());

        // Set expiration time (10 minutes)
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        String verificationUrl = link+"/verify?token=" + token;
        String emailContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <title>Verify Your Email</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;'>" +
                "    <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
                "        <tr>" +
                "            <td align='center' style='padding: 20px 0;'>" +
                "                <table width='600px' style='background-color: #ffffff; padding: 20px; border-radius: 8px; " +
                "                     box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);'>" +
                "                    <tr>" +
                "                        <td align='center' style='padding-bottom: 20px;'>" +
                "                            <h2 style='color: #333;'>Welcome to PetitAi!</h2>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td align='center' style='font-size: 16px; color: #555; padding: 10px 20px;'>" +
                "                            Thank you for signing up. Please verify your email address by clicking the button below." +
                "                        </td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td align='center' style='padding: 20px;'>" +
                "                            <a href='" + verificationUrl + "' style='background-color: #007bff; color: #ffffff; text-decoration: none; " +
                "                               padding: 12px 24px; font-size: 16px; border-radius: 5px; display: inline-block;'>Verify Email</a>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td align='center' style='font-size: 12px; color: #bbb; padding: 20px 0;'>" +
                "                            &copy; 2025 PetitAi. All rights reserved." +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(user.getEmail(), "Verify Your Email", emailContent);


        return ResponseEntity.ok("Verification email sent! Please check your inbox.");
    }

    // ✅ Verify Email
    public ResponseEntity<String> verifyEmail(String token) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);

        if (!optionalToken.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token!");
        }



        VerificationToken verificationToken = optionalToken.get();

        if (verificationToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Token expired!");
        }
        System.out.println("Hi inside verifyUser"+verificationToken.toString());

        // Create the user after verification
        Users user = new Users();
        user.setEmail(verificationToken.getEmail());
        user.setName(verificationToken.getUserName());
        user.setHashedPassword(verificationToken.getHashedPassword());
        user.setActive(true);
        userRepository.save(user);

        // Remove the token after verification
        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Email verified! You can now log in.");
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public ResponseEntity<String> login(Users user) {
        //Authentication is done in the SecurityConfig class
        //this Login is just generate the jwt token

        //this will run the authenticate method of the AuthenticationManager with our Authentication Provider
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getHashedPassword()));

        if(authentication.isAuthenticated()){
            return ResponseEntity.ok(jwtService.generateToken(user.getEmail()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
    }
}
