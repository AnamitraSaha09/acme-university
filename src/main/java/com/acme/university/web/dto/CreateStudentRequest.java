package com.acme.university.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for "Add Student" (create a student and assign them to a
 * lecturer).
 *
 * <p>(Validation requirement): add Bean Validation constraints so that
 * {@code name} and {@code surname} are not null/blank and alphanumeric only.
 * See {@link CreateLecturerRequest} for hints.
 */
public record CreateStudentRequest(

        @NotBlank(message = "Student id cannot be null/blank.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Student id must be alphanumeric")
        String studentId,

        @NotBlank(message = "Student name cannot be null/blank.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Student name must be alphanumeric")
        String name,

        @NotBlank(message = "Student surname cannot be null/blank.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Student surname must be alphanumeric")
        String surname) {
}
