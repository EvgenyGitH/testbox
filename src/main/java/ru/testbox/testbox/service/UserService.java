package ru.testbox.testbox.service;

import ru.testbox.testbox.dto.NewUser;
import ru.testbox.testbox.dto.UpdateUser;
import ru.testbox.testbox.dto.UserDto;
import ru.testbox.testbox.model.Payment;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    UserDto createUser(NewUser newUser);

    UserDto updateUser(Long id, UpdateUser updateUser);

    UserDto getUserById(Long userId);

    void deleteUserById(Long userId);

    List<UserDto> getUsers(String name, LocalDate birthday, String number, String email, int from, int size);

    UserDto sendAmount(Long userId, Payment payment);

}
