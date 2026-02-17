package com.cashflow.app.controller;

import com.cashflow.app.entity.User;
import com.cashflow.app.payload.request.LoginRequest;
import com.cashflow.app.payload.request.SignupRequest;
import com.cashflow.app.payload.response.JwtResponse;
import com.cashflow.app.payload.response.MessageResponse;
import com.cashflow.app.repository.UserRepository;
import com.cashflow.app.security.jwt.JwtUtils;
import com.cashflow.app.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
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