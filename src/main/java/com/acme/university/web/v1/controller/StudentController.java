package com.acme.university.web.v1.controller;

import com.acme.university.service.StudentService;
import com.acme.university.web.v1.dto.StudentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST resource for students.
 */
@RestController
@RequestMapping(path = "/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Returns a student by studentId, with their assigned lecturers.
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(studentService.getStudent(studentId));
    }
}
