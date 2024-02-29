package ru.testbox.testbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testbox.testbox.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUserId(Long userId);

}
