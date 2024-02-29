package ru.testbox.testbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testbox.testbox.model.Email;

import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {
    Boolean existsByEmailContainingIgnoreCase(String email);

    List<Email> findAllByUserId(Long id);
}
