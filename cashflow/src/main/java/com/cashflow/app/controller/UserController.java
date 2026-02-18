package com.cashflow.app.controller;

import com.cashflow.app.entity.User;
import com.cashflow.app.payload.response.MessageResponse;
import com.cashflow.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            User user = getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody User profileData) {
        try {
            User user = getCurrentUser();

            // Update fields
            if (profileData.getEmail() != null) {
                user.setEmail(profileData.getEmail());
            }
            if (profileData.getPhone() != null) {
                user.setPhone(profileData.getPhone());
            }
            if (profileData.getGender() != null) {
                user.setGender(profileData.getGender());
            }
            if (profileData.getAge() != null) {
                user.setAge(profileData.getAge());
            }

            // Username update is usually restricted to avoid security/consistency issues
            // but if the user provided it and it's different, we might want to check it.
            // For now, let's keep it simple as per the frontend form.
            if (profileData.getUsername() != null && !profileData.getUsername().equals(user.getUsername())) {
                if (userRepository.existsByUsername(profileData.getUsername())) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
                }
                user.setUsername(profileData.getUsername());
            }

            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
