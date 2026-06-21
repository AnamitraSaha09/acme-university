package com.acme.university.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for "Create Lecturer".
 *
 * <p>(Validation requirement): add Bean Validation constraints so that
 * {@code name} and {@code surname}:
 * <ul>
 *   <li>are not {@code null} and not blank, and</li>
 *   <li>consist only of alphanumeric characters.</li>
 * </ul>
 * Hints: {@code @NotBlank}, {@code @Pattern(regexp = "...")}. Decide whether
 * {@code lecturerId} needs constraints too. Constraints are enforced by
 * annotating the controller parameter with {@code @Valid}.
 */
public record CreateLecturerRequest(

        @NotBlank(message = "Lecturer id cannot be null/blank.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Lecturer id must be alphanumeric")
        String lecturerId,

        @NotBlank(message = "Lecturer name cannot be null/blank.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Lecturer name must be alphanumeric")
        String name,

        @NotBlank(message = "Lecturer surname cannot be null/blank.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Lecturer surname must be alphanumeric")
        String surname) {
}
