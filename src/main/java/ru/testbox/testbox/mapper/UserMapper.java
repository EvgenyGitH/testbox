package ru.testbox.testbox.mapper;

import ru.testbox.testbox.dto.NewUser;
import ru.testbox.testbox.dto.UserDto;
import ru.testbox.testbox.model.Email;
import ru.testbox.testbox.model.Mobile;
import ru.testbox.testbox.model.User;

import java.util.List;

public class UserMapper {
    public static User toUser(NewUser newUser) {
        User user = new User();
        user.setName(newUser.getName());
        user.setBirthday(newUser.getBirthday());
        user.setLogin(newUser.getLogin());
        user.setPassword(newUser.getPassword());
        return user;
    }

    public static UserDto toDto(User user, List<Mobile> mobiles, List<Email> emails, Double amount) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setBirthday(user.getBirthday());
        userDto.setLogin(user.getLogin());
        userDto.setMobile(mobiles);
        userDto.setEmail(emails);
        userDto.setAmount(amount);
        return userDto;
    }

}

