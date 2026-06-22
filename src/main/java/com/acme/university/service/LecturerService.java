package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.exception.ResourceAlreadyExistsException;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.web.v1.dto.CreateLecturerRequest;
import com.acme.university.web.v1.dto.LecturerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for lecturers.
 */
@Service
public class LecturerService {

    private final LecturerRepository lecturerRepository;

    private static final Logger log = LoggerFactory.getLogger(LecturerService.class);

    public LecturerService(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    /**
     * Creates a new lecturer.
     * @throws com.acme.university.exception.ResourceAlreadyExistsException
     * if a lecturer with the same id already exists.
     */
    @Transactional
    public LecturerResponse createLecturer(CreateLecturerRequest request) {
        if(lecturerRepository.existsByLecturerId(request.lecturerId())) {
            log.error("Lecturer with lecturerId {} already exists.", request.lecturerId());
            throw new ResourceAlreadyExistsException("Lecturer with id " + request.lecturerId() + " already exists.");
        }
        log.info("No lecturer found with lecturerId {}", request.lecturerId());
        Lecturer lecturer = lecturerRepository.save(new Lecturer(request.lecturerId(), request.name(), request.surname()));
        log.info("Created lecturer with lecturerId {}", lecturer.getLecturerId());
        return LecturerResponse.fromLecturer(lecturer);
    }

    /**
     * Retrieves a lecturer (and the students assigned to them) by business id.
     * @throws com.acme.university.exception.ResourceNotFoundException
     * if no lecturer with the given id exists.
     */
    @Transactional(readOnly = true)
    public LecturerResponse getLecturer(String lecturerId) {
        Lecturer lecturer = lecturerRepository.findByLecturerId(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer with id " + lecturerId + " not found."));
        log.info("Lecturer found with studentId {}.", lecturerId);
        return LecturerResponse.fromLecturer(lecturer);
    }
}
