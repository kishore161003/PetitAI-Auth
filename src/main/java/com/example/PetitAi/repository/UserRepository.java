package com.example.PetitAi.repository;


import com.example.PetitAi.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<Users, String> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByGoogleId(String googleId);

    List<Users> findByGender(String gender);

}
