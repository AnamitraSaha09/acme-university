package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.exception.ResourceAlreadyExistsException;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.web.v1.dto.CreateLecturerRequest;
import com.acme.university.web.v1.dto.LecturerResponse;
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

/**
 * Unit test with repository mocked
 */
@ExtendWith(MockitoExtension.class)
public class LecturerServiceTest {

    @Mock
    private LecturerRepository lecturerRepository;

    @InjectMocks
    private LecturerService lecturerService;

    private final String LECTURER_ID = "L1";
    private final String LECTURER_NAME = "Name";
    private final String LECTURER_SURNAME = "Surname";
    private final Lecturer lecturer = new Lecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);

    @Test
    void createLecturer_newLecturer() {
        when(lecturerRepository.existsByLecturerId(LECTURER_ID)).thenReturn(false);
        when(lecturerRepository.save(any(Lecturer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LecturerResponse response = lecturerService.createLecturer(
                new CreateLecturerRequest(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME));

        assertThat(response.lecturerId()).isEqualTo(LECTURER_ID);
        assertThat(response.name()).isEqualTo(LECTURER_NAME);
        assertThat(response.surname()).isEqualTo(LECTURER_SURNAME);
        assertThat(response.students()).isEmpty();
        verify(lecturerRepository).save(any(Lecturer.class));
    }

    @Test
    void createLecturer_whenAlreadyExists() {
        when(lecturerRepository.existsByLecturerId(LECTURER_ID)).thenReturn(true);

        assertThatThrownBy(() -> lecturerService.createLecturer(
                new CreateLecturerRequest(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME)))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void getLecturer_returnsResponse() {
        when(lecturerRepository.findByLecturerId(LECTURER_ID))
                .thenReturn(Optional.of(lecturer));

        LecturerResponse response = lecturerService.getLecturer(LECTURER_ID);

        assertThat(response.lecturerId()).isEqualTo(LECTURER_ID);
        assertThat(response.students()).isEmpty();
    }

    @Test
    void getLecturer_whenMissing() {
        when(lecturerRepository.findByLecturerId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lecturerService.getLecturer("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
