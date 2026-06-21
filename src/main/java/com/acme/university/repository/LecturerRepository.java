package com.acme.university.repository;

import com.acme.university.domain.Lecturer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

    Optional<Lecturer> findByLecturerId(String lecturerId);

    boolean existsByLecturerId(String lecturerId);
}
