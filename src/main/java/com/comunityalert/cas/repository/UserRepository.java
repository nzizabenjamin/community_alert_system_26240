package com.comunityalert.cas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import com.comunityalert.cas.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);


    @Query("SELECT u FROM User u WHERE u.location.type = 'VILLAGE' AND u.location.parent.parent.parent.parent.name = :provinceName")
    List<User> findUsersByProvinceName(String provinceName);

    @Query("SELECT u.location.parent.parent.parent.parent FROM User u WHERE u.id = :userId")
    Optional<String> findProvinceByUserId(String userId);
}
