package com.acme.university.web.v1.controller;

import com.acme.university.service.LecturerService;
import com.acme.university.service.StudentService;
import com.acme.university.web.v1.dto.CreateLecturerRequest;
import com.acme.university.web.v1.dto.CreateStudentRequest;
import com.acme.university.web.v1.dto.LecturerResponse;
import com.acme.university.web.v1.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST resource for lecturers (and assigning students to a lecturer).
 */
@RestController
@RequestMapping(path = "/api/v1/lecturers")
public class LecturerController {

    private final LecturerService lecturerService;
    private final StudentService studentService;

    public LecturerController(LecturerService lecturerService, StudentService studentService) {
        this.lecturerService = lecturerService;
        this.studentService = studentService;
    }

    /**
     * Returns a lecturer by lecturerId along with their assigned students if any exists.
     */
    @GetMapping("/{lecturerId}")
    public ResponseEntity<LecturerResponse> getLecturer(@PathVariable String lecturerId) {
        return ResponseEntity.ok(lecturerService.getLecturer(lecturerId));
    }

    /**
     * Creates a lecturer if it doesn't already exist, otherwise return a ResourceAlreadyExistsException.
     */
    @PostMapping
    public ResponseEntity<LecturerResponse> createLecturer(@Valid @RequestBody CreateLecturerRequest lecturerRequest) {
        LecturerResponse lecturer =  lecturerService.createLecturer(lecturerRequest);
        return ResponseEntity.created(URI.create("/api/v1/lecturers")).body(lecturer);
    }

    /**
     * Creates the student if not present and assigns it to an existing lecturer,
     * ResourceNotFoundException if the lecturer does not exist.
     */
    @PostMapping("/{lecturerId}/students")
    public ResponseEntity<StudentResponse> addStudent(
            @PathVariable String lecturerId,
            @Valid @RequestBody CreateStudentRequest studentRequest
            ) {
        StudentResponse student = studentService.addStudentToLecturer(lecturerId, studentRequest);
        return ResponseEntity.created(URI.create("/api/v1/students")).body(student);
    }
}
