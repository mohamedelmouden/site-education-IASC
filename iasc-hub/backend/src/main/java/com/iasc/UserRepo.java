package com.iasc;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface UserRepo extends JpaRepository<User,Long>{ Optional<User> findByEmail(String e); boolean existsByEmail(String e); }
