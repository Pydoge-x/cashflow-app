package com.cashflow.app.controller;

import com.cashflow.app.entity.User;
import com.cashflow.app.payload.request.CodeRequest;
import com.cashflow.app.payload.request.LoginRequest;
import com.cashflow.app.payload.request.SignupRequest;
import com.cashflow.app.payload.response.JwtResponse;
import com.cashflow.app.payload.response.MessageResponse;
import com.cashflow.app.repository.UserRepository;
import com.cashflow.app.security.jwt.JwtUtils;
import com.cashflow.app.security.services.UserDetailsImpl;
import com.cashflow.app.service.NotificationService;
import com.cashflow.app.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    VerificationCodeService verificationCodeService;

    @Autowired
    List<NotificationService> notificationServices;

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getUsername();
        if (identifier == null || identifier.isEmpty()) {
            identifier = loginRequest.getAccount();
        }

        if (identifier == null || identifier.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: username or account must be provided"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                user));
    }

    @PostMapping("/auth/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody CodeRequest codeRequest) {
        String target = codeRequest.getTarget();
        String method = codeRequest.getMethod(); // EMAIL or PHONE

        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Target (email/phone) is required"));
        }

        // Generate code
        String code = verificationCodeService.generateCode(target);

        // Find appropriate service
        NotificationService service = notificationServices.stream()
                .filter(s -> s.supports(method))
                .findFirst()
                .orElse(null);

        if (service == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Unsupported registration method"));
        }

        // Send code
        service.sendVerificationCode(target, code);

        return ResponseEntity.ok(new MessageResponse("Verification code sent successfully (Mock)"));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        // 1. Check if Username exists
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // 2. Validate Code
        String target = "EMAIL".equalsIgnoreCase(signUpRequest.getRegistrationMethod())
                ? signUpRequest.getEmail()
                : signUpRequest.getPhone();

        System.out.println("[DEBUG] Registration request - Method: [" + signUpRequest.getRegistrationMethod()
                + "], Target: [" + target + "]");

        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email or Phone is required for the chosen registration method"));
        }

        if (!verificationCodeService.verifyCode(target, signUpRequest.getCode())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or expired verification code"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPhone(signUpRequest.getPhone());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        if (signUpRequest.getGender() != null && !signUpRequest.getGender().isEmpty()) {
            user.setGender(User.Gender.valueOf(signUpRequest.getGender().toUpperCase()));
        }
        if (signUpRequest.getAge() != null) {
            user.setAge(signUpRequest.getAge());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
