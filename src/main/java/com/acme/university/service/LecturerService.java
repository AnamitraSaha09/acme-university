package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.exception.ResourceAlreadyExistsException;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.web.dto.CreateLecturerRequest;
import com.acme.university.web.dto.LecturerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for lecturers.
 */
@Service
public class LecturerService {

    private final LecturerRepository lecturerRepository;

    public LecturerService(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    /**
     * Creates a new lecturer.
     *
     * @throws com.acme.university.exception.ResourceAlreadyExistsException
     *         if a lecturer with the same id already exists.
     */
    @Transactional
    public LecturerResponse createLecturer(CreateLecturerRequest request) {
        // Implements
        //  - reject creation if a lecturer with this lecturerId already exists
        //  - persist the new lecturer
        //  - map the saved entity to a LecturerResponse
        if(lecturerRepository.existsByLecturerId(request.lecturerId())) {
            throw new ResourceAlreadyExistsException("Lecturer with id " + request.lecturerId() + " already exists");
        }
        Lecturer lecturer = new Lecturer(request.lecturerId(), request.name(), request.surname());
        return LecturerResponse.fromLecturer(lecturerRepository.save(lecturer));
    }

    /**
     * Retrieves a lecturer (and the students assigned to them) by business id.
     *
     * @throws com.acme.university.exception.ResourceNotFoundException
     *         if no lecturer with the given id exists.
     */
    @Transactional(readOnly = true)
    public LecturerResponse getLecturer(String lecturerId) {
        // Implements
        //  - look the lecturer up by lecturerId
        //  - map to a LecturerResponse including the assigned students
        Lecturer lecturer = lecturerRepository.findByLecturerId(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer with id " + lecturerId + " not found"));
        return LecturerResponse.fromLecturer(lecturer);
    }
}
