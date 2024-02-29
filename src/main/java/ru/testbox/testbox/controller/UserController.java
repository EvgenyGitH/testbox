package ru.testbox.testbox.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.testbox.testbox.dto.NewUser;
import ru.testbox.testbox.dto.UpdateUser;
import ru.testbox.testbox.dto.UserDto;
import ru.testbox.testbox.model.Payment;
import ru.testbox.testbox.service.UserService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody @Valid NewUser newUser) {
        log.info("Create User");
        return userService.createUser(newUser);
    }

    @PatchMapping
    public UserDto updateUser(@RequestHeader("X-TestBox-User-Id") Long userId,
                              @RequestBody UpdateUser updateUser) {
        log.info("Update User");
        return userService.updateUser(userId, updateUser);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Get user by Id");
        return userService.getUserById(userId);
    }

    @DeleteMapping
    public void deleteUserById(@RequestHeader("X-TestBox-User-Id") Long userId) {
        log.info("Delete User by id");
        userService.deleteUserById(userId);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthday,
                                  @RequestParam(required = false) String number,
                                  @RequestParam(required = false) String email,
                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Get Users by param");
        return userService.getUsers(name, birthday, number, email, from, size);
    }

    @PatchMapping("/payment")
    public UserDto sendAmount(@RequestHeader("X-TestBox-User-Id") Long userId,
                              @RequestBody Payment payment) {
        log.info("Payment");
        return userService.sendAmount(userId, payment);
    }

}
