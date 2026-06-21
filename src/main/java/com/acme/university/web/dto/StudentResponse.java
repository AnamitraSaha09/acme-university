package com.acme.university.web.dto;

import com.acme.university.domain.Student;

import java.util.List;

/**
 * Response body for "Get Student": the student's identity plus the list of
 * assigned lecturers.
 */
public record StudentResponse(
        String studentId,
        String name,
        String surname,
        List<AssignedLecturer> lecturers) {

    /**
     * Minimal view of a lecturer assigned to the student.
     */
    public record AssignedLecturer(
            String lecturerId,
            String name,
            String surname) {
    }

    public static StudentResponse fromStudent(Student student) {
        List<AssignedLecturer> assignedLecturers = student.getLecturers().stream().map(
                lecturer ->
                        new AssignedLecturer(lecturer.getLecturerId(), lecturer.getName(), lecturer.getSurname())
        ).toList();
        return new StudentResponse(student.getStudentId(), student.getName(), student.getSurname(), assignedLecturers);
    }
}
