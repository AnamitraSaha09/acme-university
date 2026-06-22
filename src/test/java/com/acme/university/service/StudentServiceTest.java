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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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

    private final String LECTURER_ID = "L1";
    private final String LECTURER_NAME = "NameL1";
    private final String LECTURER_SURNAME = "SurnameL1";
    private final String STUDENT_ID = "S1";
    private final String STUDENT_NAME = "NameS1";
    private final String STUDENT_SURNAME = "SurnameS1";
    private final Lecturer lecturer = new Lecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);
    private final Student student = new Student(STUDENT_ID, STUDENT_NAME, STUDENT_SURNAME);

    @Test
    void addStudent_whenNew() {
        when(lecturerRepository.findByLecturerId(LECTURER_ID)).thenReturn(Optional.of(lecturer));
        when(studentRepository.existsByStudentId(STUDENT_ID)).thenReturn(false);

        StudentResponse response = studentService.addStudentToLecturer(
                LECTURER_ID, new CreateStudentRequest(STUDENT_ID, STUDENT_NAME, STUDENT_SURNAME));

        assertThat(response.studentId()).isEqualTo(STUDENT_ID);
        assertThat(response.lecturers()).hasSize(1);
        assertThat(response.lecturers().getFirst().lecturerId()).isEqualTo(LECTURER_ID);

        verify(studentRepository, never()).save(any(Student.class));
        verify(lecturerRepository).saveAndFlush(lecturer);
    }

    @Test
    void addStudent_whenStudentExists() {
        when(lecturerRepository.findByLecturerId(LECTURER_ID)).thenReturn(Optional.of(lecturer));
        when(studentRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.of(student));
        when(studentRepository.existsByStudentId(STUDENT_ID)).thenReturn(true);

        StudentResponse response = studentService.addStudentToLecturer(
                LECTURER_ID, new CreateStudentRequest(STUDENT_ID, STUDENT_NAME, STUDENT_SURNAME));

        assertThat(response.studentId()).isEqualTo(STUDENT_ID);
        assertThat(response.lecturers()).hasSize(1);
        verify(studentRepository, never()).save(any());
        verify(lecturerRepository).saveAndFlush(lecturer);
    }

    @Test
    void addStudent_whenLecturerMissing() {
        when(lecturerRepository.findByLecturerId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.addStudentToLecturer(
                "missing", new CreateStudentRequest(STUDENT_ID, STUDENT_NAME, STUDENT_SURNAME)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(studentRepository, never()).save(any());
        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void getStudent_returnsResponse() {
        when(studentRepository.findByStudentId(STUDENT_ID))
                .thenReturn(Optional.of(student));

        StudentResponse response = studentService.getStudent(STUDENT_ID);

        assertThat(response.studentId()).isEqualTo(STUDENT_ID);
        assertThat(response.lecturers()).isEmpty();
    }

    @Test
    void getStudent_whenMissing() {
        when(studentRepository.findByStudentId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudent("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addStudent_whenExistingStudentDataMismatch_throwsDataMismatch() {
        when(lecturerRepository.findByLecturerId(LECTURER_ID)).thenReturn(Optional.of(lecturer));
        when(studentRepository.existsByStudentId(STUDENT_ID)).thenReturn(true);
        when(studentRepository.findByStudentId(STUDENT_ID)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.addStudentToLecturer(
                LECTURER_ID, new CreateStudentRequest(STUDENT_ID, "differentName", STUDENT_SURNAME)))
                .isInstanceOf(DataMismatchException.class);

        verify(lecturerRepository, never()).saveAndFlush(any());
    }

    @Test
    void addStudent_whenConcurrentDuplicate_translatesToResourceAlreadyExists() {
        when(lecturerRepository.findByLecturerId(LECTURER_ID)).thenReturn(Optional.of(lecturer));
        when(studentRepository.existsByStudentId(STUDENT_ID)).thenReturn(false);
        when(lecturerRepository.saveAndFlush(any(Lecturer.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> studentService.addStudentToLecturer(
                LECTURER_ID, new CreateStudentRequest(STUDENT_ID, STUDENT_NAME, STUDENT_SURNAME))
        ).isInstanceOf(ResourceAlreadyExistsException.class);
    }
}
