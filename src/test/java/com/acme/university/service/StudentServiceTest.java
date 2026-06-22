package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.domain.Student;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.acme.university.web.v1.dto.CreateStudentRequest;
import com.acme.university.web.v1.dto.StudentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Unit test with repository mocked
 */
@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void addStudent_whenNew() {
        Lecturer lecturer = new Lecturer("L1", "NameL1", "SurnameL1");
        when(lecturerRepository.findByLecturerId("L1")).thenReturn(Optional.of(lecturer));
        when(studentRepository.findByStudentId("S1")).thenReturn(Optional.empty());
        when(studentRepository.existsByStudentId("S1")).thenReturn(false);

        StudentResponse response = studentService.addStudentToLecturer(
                "L1", new CreateStudentRequest("S1", "NameS1", "SurnameS1"));

        assertThat(response.studentId()).isEqualTo("S1");
        assertThat(response.lecturers()).hasSize(1);
        assertThat(response.lecturers().getFirst().lecturerId()).isEqualTo("L1");
        // should be persisted via JPA
        verify(studentRepository, never()).save(any(Student.class));
        verify(lecturerRepository).save(lecturer);
    }

    @Test
    void addStudent_whenStudentExists() {
        Lecturer lecturer = new Lecturer("L1", "NameL1", "SurnameL1");
        Student existing = new Student("S1", "NameS1", "SurnameS1");
        when(lecturerRepository.findByLecturerId("L1")).thenReturn(Optional.of(lecturer));
        when(studentRepository.findByStudentId("S1")).thenReturn(Optional.of(existing));
        when(studentRepository.existsByStudentId("S1")).thenReturn(true);

        StudentResponse response = studentService.addStudentToLecturer(
                "L1", new CreateStudentRequest("S1", "NameS1", "SurnameS1"));

        assertThat(response.studentId()).isEqualTo("S1");
        assertThat(response.lecturers()).hasSize(1);
        verify(studentRepository, never()).save(any());
        verify(lecturerRepository).save(lecturer);
    }

    @Test
    void addStudent_whenLecturerMissing() {
        when(lecturerRepository.findByLecturerId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.addStudentToLecturer(
                "missing", new CreateStudentRequest("S1", "NameS1", "SurnameS1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(studentRepository, never()).save(any());
        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void getStudent_returnsResponse() {
        when(studentRepository.findByStudentId("S1"))
                .thenReturn(Optional.of(new Student("S1", "NameS1", "SurnameS1")));

        StudentResponse response = studentService.getStudent("S1");

        assertThat(response.studentId()).isEqualTo("S1");
        assertThat(response.lecturers()).isEmpty();
    }

    @Test
    void getStudent_whenMissing() {
        when(studentRepository.findByStudentId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudent("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
