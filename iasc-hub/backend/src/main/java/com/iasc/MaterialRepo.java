package com.iasc;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface MaterialRepo extends JpaRepository<Material,Long>{ List<Material> findBySubjectIdOrderByUploadedAtAsc(Long id); }
