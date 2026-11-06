package ru.delivery.courierdelivery.service;

import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.domain.Roles;
import ru.delivery.courierdelivery.model.User;
import ru.delivery.courierdelivery.repository.UserRepository;

@Service
@Transactional
public class AuthService {

    private static final Pattern STRONG = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String name, String email, String rawPassword) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже зарегистрирован");
        }
        if (!STRONG.matcher(rawPassword).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пароль: минимум 8 символов, хотя бы одна заглавная, строчная буква и цифра"
            );
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        // регистрация не выдаёт ADMIN
        user.setRole(Roles.SENDER);
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }
}
