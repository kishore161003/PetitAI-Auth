package com.example.PetitAi.controller;

import org.springframework.http.ResponseEntity;
import com.example.PetitAi.entity.Users;
import com.example.PetitAi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Users user){
        return userService.registerUser(user);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        System.out.println("Verifying user");
        return userService.verifyEmail(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Users user){
        return userService.login(user);
    }

    @GetMapping
    public List<Users> getAllUsers() {
        System.out.println("Getting all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<Users> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @GetMapping("/gender/{id}")
    public List<Users> getUsersByGender(@PathVariable String id) {
        return userService.getUsersByGender(id);
    }

    @GetMapping("/email/{id}")
    public Optional<Users> getUserByEmail(@PathVariable String id) {
        return userService.getUserByEmail(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }
}

@RestController
 class HomeController {
    @GetMapping("/")
    public String home() {
        return "Welcome to PetitAi!";
    }

}