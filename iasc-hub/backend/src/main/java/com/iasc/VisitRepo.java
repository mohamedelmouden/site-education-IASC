package com.iasc;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface VisitRepo extends JpaRepository<Visit,Long>{ List<Visit> findByUserIdOrderByAtDesc(Long id); void deleteByUserId(Long id); void deleteByMaterialId(Long id); }
