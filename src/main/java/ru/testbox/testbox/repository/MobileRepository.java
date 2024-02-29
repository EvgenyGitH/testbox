package ru.testbox.testbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testbox.testbox.model.Mobile;

import java.util.List;

public interface MobileRepository extends JpaRepository<Mobile, Long> {
    Boolean existsByNumber(String number);

    List<Mobile> findAllByUserId(Long id);
}
