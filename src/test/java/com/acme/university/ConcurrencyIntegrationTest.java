package com.acme.university;

import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Concurrency / race-condition tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "rate-limit.capacity=10000",
        "rate-limit.refill-interval=1s"
})
class ConcurrencyIntegrationTest {

    private static final int THREADS = 16;
    private final String LECTURER_ID = "L1";
    private final String STUDENT_ID = "S1";
    private final String STUDENT_NAME = "NameS1";
    private final String STUDENT_SURNAME = "SurnameS1";
    private final String KEY_NAME = "name";
    private final String KEY_SURNAME = "surname";
    private final String KEY_LECTURER_ID = "lecturerId";
    private final String KEY_STUDENT_ID = "studentId";
    private final Map<String, String> STUDENT_CONTENT = Map.of(
            KEY_STUDENT_ID, STUDENT_ID,
            KEY_NAME, STUDENT_NAME,
            KEY_SURNAME, STUDENT_SURNAME
    );
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LecturerRepository lecturerRepository;
    @Autowired
    private StudentRepository studentRepository;

    private List<Integer> fireConcurrently(Callable<Integer> task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return task.call();
            }));
        }
        start.countDown();

        List<Integer> statuses = new ArrayList<>();
        try {
            for (Future<Integer> f : futures) {
                statuses.add(f.get(20, TimeUnit.SECONDS));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            pool.shutdownNow();
        }
        return statuses;
    }

    private int postStudent(String lecturerId, Map<String, String> content) throws Exception {
        return mockMvc.perform(post("/api/v1/lecturers/" + lecturerId + "/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(content)))
                .andReturn().getResponse().getStatus();
    }

    private void createLecturer(String id) throws Exception {
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lecturerContent(id))))
                .andExpect(status().isCreated());
    }

    private Map<String, String> lecturerContent(String id) {
        return Map.of(KEY_LECTURER_ID, id, KEY_NAME, "Name" + id, KEY_SURNAME, "Surname" + id);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @BeforeEach
    void cleanDatabase() {
        lecturerRepository.deleteAll();
        studentRepository.deleteAll();
    }

    /**
     * Same new student, assigned via many lecturers at once.
     * The unique student_id constraint must guarantee a single student row;
     * the losers of the create race return 409, never 5xx.
     */
    @Test
    void concurrentlyCreatingSameStudent_acrossLecturers_createsExactlyOneStudent() throws Exception {
        for (int i = 0; i < THREADS; i++) {
            createLecturer("L" + i);
        }

        AtomicInteger seq = new AtomicInteger();
        List<Integer> statuses = fireConcurrently(() ->
                postStudent("L" + seq.getAndIncrement(), STUDENT_CONTENT));

        // Exactly one student row, regardless of how the race resolved.
        assertThat(studentRepository.count()).isEqualTo(1);

        // Every response is either created or a clean conflict — no 500s.
        assertThat(statuses).allMatch(s -> s == 201 || s == 409);

        // The number of successful assignments must match the lecturers that got a 201.
        long successes = statuses.stream().filter(s -> s == 201).count();
        assertThat(successes).isGreaterThanOrEqualTo(1);
        mockMvc.perform(get("/api/v1/students/" + STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lecturers.length()").value((int) successes));
    }

    /**
     * Same student via the same lecturer at once.
     * One student row, one assignment, no 5xx.
     */
    @Test
    void concurrentlyAddingSameStudent_toSameLecturer_isConsistent() throws Exception {
        createLecturer(LECTURER_ID);

        List<Integer> statuses = fireConcurrently(() -> postStudent(LECTURER_ID, STUDENT_CONTENT));

        assertThat(studentRepository.count()).isEqualTo(1);
        assertThat(statuses).allMatch(s -> s == 201 || s == 409);

        // The lecturer must end up with exactly one assignment to the student.
        mockMvc.perform(get("/api/v1/lecturers/" + LECTURER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students.length()").value(1))
                .andExpect(jsonPath("$.students[0].studentId").value(STUDENT_ID));
    }

    /**
     * Same lecturerId created by many threads at once.
     * Exactly one creation wins, the rest are 409, never 5xx.
     */
    @Test
    void concurrentlyCreatingSameLecturer_createsExactlyOne() throws Exception {
        String body = json(lecturerContent(LECTURER_ID));

        List<Integer> statuses = fireConcurrently(() -> mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getStatus());

        assertThat(lecturerRepository.count()).isEqualTo(1);
        assertThat(statuses).allMatch(s -> s == 201 || s == 409);
        assertThat(statuses.stream().filter(s -> s == 201).count()).isEqualTo(1);
    }
}