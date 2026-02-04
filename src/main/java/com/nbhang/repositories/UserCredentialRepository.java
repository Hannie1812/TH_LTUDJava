package com.nbhang.repositories;

import com.nbhang.entities.UserCredential;
import com.nbhang.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    List<UserCredential> findByUser(User user);

    Optional<UserCredential> findByCredentialId(String credentialId);
}
