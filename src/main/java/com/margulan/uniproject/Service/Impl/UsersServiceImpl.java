package com.margulan.uniproject.Service.Impl;

import com.margulan.uniproject.Exception.DuplicateEmailException;
import com.margulan.uniproject.Exception.ResourceNotFoundException;
import com.margulan.uniproject.Exception.UserNotFoundException;
import com.margulan.uniproject.Mapper.UserMapper;
import com.margulan.uniproject.Model.Dto.UserDto;
import com.margulan.uniproject.Model.User;
import com.margulan.uniproject.Repository.UsersRepository;
import com.margulan.uniproject.Service.RedisService;
import com.margulan.uniproject.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersServiceImpl implements UsersService {

    private static final String USERS_CACHE_KEY = "#userId";

//    @Autowired
//    JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void createUser(UserDto userDto) {

        if (usersRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Duplicate email");
        } else {
            User user = userMapper.toEntity(userDto);
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setRole("USER");
            usersRepository.save(user);
        }
    }

    @Override
    public List<UserDto> getAllUsers() {
        try {
            List<User> users = usersRepository.findAll();
            List<UserDto> userDtos = new ArrayList<>();
            for (User user : users) {
                userDtos.add(userMapper.toDto(user));
            }
            return userDtos;
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot get users.");
        }
    }

    @Override
    public void deleteUser(String id) {
        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("User not found: id - " + id);
        }
    }

    @Override
    public User login(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User foundUser = usersRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("Incorrect email or password")
        );

        if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            return foundUser;
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @Override
    public String getLoggedUsernameByEmail(String email) {
        return usersRepository.findByEmail(email).get().getUsername();
    }

    @Override
    public User findByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found -> incorrect email.")
        );
    }

    @Override
    public void resetUserWithNewPassword(User user) {
        usersRepository.save(user);
    }

    @Override
    public Authentication getLoggedUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
