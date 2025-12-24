package ru.practicum.main.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UpdateUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.exception.AlreadyExistsException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Creating user with email: {}", newUserRequest.getEmail());

        checkEmailUniqueness(null, newUserRequest.getEmail());

        User user = UserMapper.toEntity(newUserRequest);
        User savedUser = userRepository.save(user);

        log.info("User created with id: {}", savedUser.getId());
        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest updateUserRequest) {
        log.info("Updating user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        if (updateUserRequest.getName() != null && !updateUserRequest.getName().isBlank()) {
            user.setName(updateUserRequest.getName());
        }

        if (updateUserRequest.getEmail() != null &&
                !updateUserRequest.getEmail().isBlank() &&
                !updateUserRequest.getEmail().equals(user.getEmail())) {

            checkEmailUniqueness(userId, updateUserRequest.getEmail());
            user.setEmail(updateUserRequest.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("User with id: {} updated", userId);

        return UserMapper.toDto(updatedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Getting users with ids: {}, from: {}, size: {}", ids, from, size);

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findAllById(ids);
        }

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        userRepository.deleteById(userId);
        log.info("User with id: {} deleted", userId);
    }

    private void checkEmailUniqueness(Long userId, String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            if (userId == null || !existingUser.get().getId().equals(userId)) {
                throw new AlreadyExistsException("Email '" + email + "' already exists");
            }
        }
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("Parameter 'from' must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be > 0");
        }
    }
}