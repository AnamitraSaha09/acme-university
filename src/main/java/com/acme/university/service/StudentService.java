package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.domain.Student;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.acme.university.web.dto.CreateStudentRequest;
import com.acme.university.web.dto.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for students.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public StudentService(StudentRepository studentRepository, LecturerRepository lecturerRepository) {
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    /**
     * Creates a student (if not already present) and assigns them to an
     * existing lecturer.
     *
     * @throws com.acme.university.exception.ResourceNotFoundException if the target lecturer does not exist.
     */
    @Transactional
    public StudentResponse addStudentToLecturer(String lecturerId, CreateStudentRequest request) {
        // Implements
        //  - fail if the lecturer does not exist
        //  - create the student only if one with this studentId is not present,
        //    otherwise reuse the existing student
        //  - assign the student to the lecturer (keep both sides in sync)
        //  - map to a StudentResponse including the assigned lecturers
        Lecturer lecturer = lecturerRepository.findByLecturerId(lecturerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Lecturer with id: " + lecturerId
                                +" not found while assigning to student: " + request.studentId()));

        Student student = studentRepository.findByStudentId(request.studentId())
                .orElseGet(
                        () -> new Student(request.studentId(), request.name(), request.surname())
                );
        lecturer.assignUniqueStudent(student);
        lecturerRepository.save(lecturer);
        return StudentResponse.fromStudent(student);
    }

    /**
     * Retrieves a student (and the lecturers assigned to them) by business id.
     *
     * @throws com.acme.university.exception.ResourceNotFoundException if no student with the given id exists.
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudent(String studentId) {
        // Implements
        //  - look the student up by studentId
        //  - map to a StudentResponse including the assigned lecturers
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow( () -> new ResourceNotFoundException("Student with id: " + studentId + " not found."));
        return StudentResponse.fromStudent(student);
    }
}
