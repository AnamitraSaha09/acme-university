package com.acme.university.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LecturerTest {

    private final String LECTURER_ID = "L1";
    private final String LECTURER_NAME = "NameL1";
    private final String LECTURER_SURNAME = "SurnameL1";
    private final String STUDENT_ID = "S1";
    private final String STUDENT_NAME = "NameS1";
    private final String STUDENT_SURNAME = "SurnameS1";
    private final Lecturer lecturer = new Lecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);
    private final Student student = new Student(STUDENT_ID, STUDENT_NAME, STUDENT_SURNAME);

    @Test
    void assignStudent_linksBothSides() {
        lecturer.assignUniqueStudent(student);

        assertThat(lecturer.getStudents()).containsExactly(student);
        assertThat(student.getLecturers()).containsExactly(lecturer);
    }

    @Test
    void assignStudent_isIdempotent() {
        lecturer.assignUniqueStudent(student);
        lecturer.assignUniqueStudent(student);

        assertThat(lecturer.getStudents()).hasSize(1);
        assertThat(student.getLecturers()).hasSize(1);
    }

    @Test
    void equalsAndHashCode_useBusinessId() {
        Lecturer one = new Lecturer("L1", "N1", "S1");
        Lecturer sameId = new Lecturer("L1", "N2", "S2");
        Lecturer otherId = new Lecturer("L2", "N1", "S1");

        assertThat(one).isEqualTo(sameId);
        assertThat(one).hasSameHashCodeAs(sameId);
        assertThat(one).isNotEqualTo(otherId);
    }
}
