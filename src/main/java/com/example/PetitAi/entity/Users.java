package com.example.PetitAi.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users") // MongoDB collection name
public class Users {

    @Id
    private String id;  // MongoDB _id (automatically generated)

    private String name;

    private String email;

    private String hashedPassword; // Null for Google-auth users

    private String googleId; // Google Sign-In users

    private Date dob;

    private String gender; // Male, Female, Other

    private String profilePic;

    private String address;

    private String phoneNo;

    private String bio;

    private boolean isActive = true;

    @DBRef
    private List<Petition> petitionsCreated; // References to Petition IDs

    @CreatedDate
    private Date createdAt; // Automatically set

    @LastModifiedDate
    private Date updatedAt; // Automatically set

}
