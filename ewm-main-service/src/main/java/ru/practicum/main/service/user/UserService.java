package ru.practicum.main.service.user;

import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UpdateUserRequest;
import ru.practicum.main.dto.user.UserDto;
import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest newUserRequest);

    UserDto updateUser(Long userId, UpdateUserRequest updateUserRequest); // НОВЫЙ метод

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);
}