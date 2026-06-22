package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.domain.Student;
import com.acme.university.exception.DataMismatchException;
import com.acme.university.exception.ResourceAlreadyExistsException;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.acme.university.web.v1.dto.CreateStudentRequest;
import com.acme.university.web.v1.dto.StudentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Business logic for students.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    public StudentService(StudentRepository studentRepository, LecturerRepository lecturerRepository) {
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    /**
     * Creates a student (if not already present) and assigns them to an existing lecturer.
     * @throws com.acme.university.exception.ResourceNotFoundException if the target lecturer does not exist.
     */
    @Transactional
    public StudentResponse addStudentToLecturer(String lecturerId, CreateStudentRequest request) {
        Lecturer lecturer = lecturerRepository.findByLecturerId(lecturerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Lecturer with id: " + lecturerId
                                +" not found while assigning to student: " + request.studentId()));
        log.info("Lecturer found {}", lecturerId);
        Student student;
        if(studentRepository.existsByStudentId(request.studentId()))
        {
            log.info("Student found with studentId {}", request.studentId());
            student = studentRepository.findByStudentId(request.studentId()).get();
            if(!Objects.equals(student.getName(), request.name()) || !Objects.equals(student.getSurname(), request.surname())) {
                log.error("Student name and surname do not match do not match with existing student with studentId {}", request.studentId());
                throw new DataMismatchException("Student name or surname do not match with the existing student with the same id.");
            }
        } else {
            log.warn("Student not found, therefore creating Student with studentId {}", request.studentId());
            student = new Student(request.studentId(), request.name(), request.surname());
            log.info("Creating new student with studentId {}", request.studentId());
        }
        try {
            lecturer.assignUniqueStudent(student);
            lecturerRepository.saveAndFlush(lecturer);
            log.info("Lecturer {} assigned to student {} persists.", lecturer.getLecturerId(), student.getStudentId());
        } catch (DataIntegrityViolationException e) {
            log.error("Student with id {} is already assigned to Lecturer with id {}.", request.studentId(), lecturerId);
            throw new ResourceAlreadyExistsException("Student with id " + request.studentId()
                    + " is already assigned to Lecturer with id " + lecturerId);
        }
        return StudentResponse.fromStudent(student);
    }

    /**
     * Retrieves a student (and the lecturers assigned to them) by business id.
     * @throws com.acme.university.exception.ResourceNotFoundException if no student with the given id exists.
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudent(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow( () -> new ResourceNotFoundException("Student with id: " + studentId + " not found."));
        log.info("Student found with studentId {}", studentId);
        return StudentResponse.fromStudent(student);
    }
}
