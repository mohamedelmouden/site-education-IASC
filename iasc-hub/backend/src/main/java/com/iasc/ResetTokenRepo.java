package com.iasc;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface ResetTokenRepo extends JpaRepository<ResetToken,Long>{ Optional<ResetToken> findByToken(String t); void deleteByUserId(Long id); }
