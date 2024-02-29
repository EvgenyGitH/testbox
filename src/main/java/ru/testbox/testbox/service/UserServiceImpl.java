package ru.testbox.testbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.testbox.testbox.dto.NewUser;
import ru.testbox.testbox.dto.UpdateUser;
import ru.testbox.testbox.dto.UserDto;
import ru.testbox.testbox.exception.DataConflictException;
import ru.testbox.testbox.exception.NotFoundException;
import ru.testbox.testbox.mapper.UserMapper;
import ru.testbox.testbox.model.*;
import ru.testbox.testbox.repository.AccountRepository;
import ru.testbox.testbox.repository.EmailRepository;
import ru.testbox.testbox.repository.MobileRepository;
import ru.testbox.testbox.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MobileRepository mobileRepository;
    private final EmailRepository emailRepository;
    private final AccountRepository accountRepository;

    @Override
    public UserDto createUser(NewUser newUser) {
        User user = UserMapper.toUser(newUser);
        if (userRepository.existsByLoginContainingIgnoreCase(newUser.getLogin())) {
            throw new DataConflictException("Login is used");
        }
        if (mobileRepository.existsByNumber(newUser.getMobile())) {
            throw new DataConflictException("Mobile is used");
        }
        if (emailRepository.existsByEmailContainingIgnoreCase(newUser.getEmail())) {
            throw new DataConflictException("Email is used");
        }
        User savedUser = userRepository.save(user);
        Mobile mobile = Mobile.builder()
                .userId(savedUser.getId())
                .number(newUser.getMobile())
                .build();
        mobileRepository.save(mobile);
        Email email = Email.builder()
                .userId(savedUser.getId())
                .email(newUser.getEmail())
                .build();
        emailRepository.save(email);
        Account account = Account.builder()
                .userId(savedUser.getId())
                .amount(newUser.getAmount())
                .updateAmountTime(LocalDateTime.now())
                .build();
        accountRepository.save(account);

        List<Mobile> mobiles = mobileRepository.findAllByUserId(savedUser.getId());
        List<Email> emails = emailRepository.findAllByUserId(savedUser.getId());

        return UserMapper.toDto(savedUser, mobiles, emails, newUser.getAmount());
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUser updateUser) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User ID: " + userId + " not found"));

        switch (updateUser.getAction()) {
            case ADD:
                if (updateUser.getMobile() != null) {
                    if (mobileRepository.existsByNumber(updateUser.getMobile())) {
                        throw new DataConflictException("Mobile is used");
                    }
                    Mobile mobile = Mobile.builder()
                            .userId(userId)
                            .number(updateUser.getMobile())
                            .build();
                    mobileRepository.save(mobile);
                }
                if (updateUser.getEmail() != null) {
                    if (emailRepository.existsByEmailContainingIgnoreCase(updateUser.getEmail())) {
                        throw new DataConflictException("Email is used");
                    }
                    Email email = Email.builder()
                            .userId(userId)
                            .email(updateUser.getEmail())
                            .build();
                    emailRepository.save(email);
                }
                break;
            case DELETE:
                if (updateUser.getMobile() != null) {
                    List<Mobile> mobilesList = mobileRepository.findAllByUserId(userId);
                    if (mobilesList.size() > 1) {
                        List<Mobile> filterMobiles = mobilesList.stream()
                                .filter(m -> m.getNumber().equals(updateUser.getMobile()))
                                .collect(Collectors.toList());
                        if (filterMobiles.isEmpty()) {
                            throw new NotFoundException("Mobile number: " + updateUser.getMobile() + " not found");
                        }
                        mobileRepository.deleteById(filterMobiles.get(0).getId());

                    } else {
                        throw new DataConflictException("Can't delete. Mobile number less 1");
                    }
                }
                if (updateUser.getEmail() != null) {
                    List<Email> emailsList = emailRepository.findAllByUserId(userId);
                    if (emailsList.size() > 1) {
                        List<Email> filterEmails = emailsList.stream()
                                .filter(e -> e.getEmail().equals(updateUser.getEmail()))
                                .collect(Collectors.toList());
                        if (filterEmails.isEmpty()) {
                            throw new NotFoundException("Email: " + updateUser.getEmail() + " not found");
                        }
                        emailRepository.deleteById(filterEmails.get(0).getId());

                    } else {
                        throw new DataConflictException("Can't delete. Email number less 1");
                    }
                }
                break;
        }
        List<Mobile> mobiles = mobileRepository.findAllByUserId(userId);
        List<Email> emails = emailRepository.findAllByUserId(userId);
        Double userAmout = checkAmount(userId);

        return UserMapper.toDto(user, mobiles, emails, userAmout);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User ID: " + userId + " not found"));
        List<Mobile> mobiles = mobileRepository.findAllByUserId(userId);
        List<Email> emails = emailRepository.findAllByUserId(userId);
        Double userAmout = checkAmount(userId);
        return UserMapper.toDto(user, mobiles, emails, userAmout);
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User ID: " + userId + " not found");
        }
        userRepository.deleteById(userId);

    }

    @Override
    public List<UserDto> getUsers(String name, LocalDate birthday, String number, String email, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users = userRepository.getUsers(name, birthday, number, email, pageable);

        return users.stream()
                .map(user -> UserMapper.toDto(user, mobileRepository.findAllByUserId(user.getId()),
                        emailRepository.findAllByUserId(user.getId()), checkAmount(user.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto sendAmount(Long userId, Payment payment) {
        Long recipientId = payment.getRecipientId();

        if (!userRepository.existsById(recipientId)) {
            throw new NotFoundException("User ID: " + recipientId + " not found");
        }
        Double balance = checkAmount(userId);
        if (balance < payment.getPaymentAmount()) {
            throw new DataConflictException("Not sufficient funds");
        }
        Double endBalance = balance - payment.getPaymentAmount();
        Account account = accountRepository.findByUserId(userId);
        Account amountToSave = Account.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .amount(endBalance)
                .updateAmountTime(account.getUpdateAmountTime())
                .build();
        accountRepository.save(amountToSave);

        //save Recipient's amount
        Double balanceRecipient = checkAmount(recipientId);
        Double endBalanceRecipient = balanceRecipient + payment.getPaymentAmount();
        Account accountRecipient = accountRepository.findByUserId(recipientId);
        Account amountRecipientToSave = Account.builder()
                .id(accountRecipient.getId())
                .userId(accountRecipient.getUserId())
                .amount(endBalanceRecipient)
                .updateAmountTime(LocalDateTime.now())
                .build();
        accountRepository.save(amountRecipientToSave);

        User user = userRepository.findById(userId).get();
        List<Mobile> mobiles = mobileRepository.findAllByUserId(userId);
        List<Email> emails = emailRepository.findAllByUserId(userId);

        return UserMapper.toDto(user, mobiles, emails, endBalance);
    }


    public Double checkAmount(Long userId) {
        double percent = 5.00;
        double maxPercent = 207.00;
        Double fullAmount;
        Double currentAmont;
        Double maxAmountWithPercent;
        long duration;
        Account account = accountRepository.findByUserId(userId);
        duration = Duration.between(account.getUpdateAmountTime(), LocalDateTime.now()).toMinutes();
        currentAmont = account.getAmount();
        maxAmountWithPercent = currentAmont * (1 + maxPercent / 100);
        Double calculatedAmont = currentAmont * Math.pow(1 + percent / 100, duration);
        if (calculatedAmont > maxAmountWithPercent) {
            fullAmount = maxAmountWithPercent;
        } else {
            fullAmount = calculatedAmont;
        }
        return fullAmount;
    }
}