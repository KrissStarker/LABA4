package ru.delivery.courierdelivery.service;

import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.delivery.courierdelivery.domain.Roles;
import ru.delivery.courierdelivery.model.User;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.ShipmentRepository;
import ru.delivery.courierdelivery.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of(Roles.ADMIN, Roles.OPERATOR, Roles.SENDER);
    private static final Set<String> ALLOWED_STATUS = Set.of("ACTIVE", "BLOCKED");

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            ShipmentRepository shipmentRepository,
            ShipmentAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    public User create(String name, String email, String role, String status) {
        String normalizedEmail = normalizeEmail(email);
        ensureEmailFree(normalizedEmail, null);
        String normalizedRole = normalizeRole(role);
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setRole(normalizedRole);
        user.setStatus(normalizeStatus(status));
        // временный пароль для учётки, созданной админом; сменить через регистрацию нельзя
        user.setPasswordHash(passwordEncoder.encode("ChangeMe1"));
        return userRepository.save(user);
    }

    public User update(Long id, String name, String email, String role, String status) {
        User user = findById(id);
        String normalizedEmail = normalizeEmail(email);
        ensureEmailFree(normalizedEmail, id);
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setRole(normalizeRole(role));
        if (status != null && !status.isBlank()) {
            user.setStatus(normalizeStatus(status));
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        List<Long> shipmentIds = shipmentRepository.findBySender_Id(id).stream()
                .map(shipment -> shipment.getId())
                .toList();
        if (!shipmentIds.isEmpty()) {
            assignmentRepository.deleteByShipmentIds(shipmentIds);
            shipmentRepository.deleteBySenderId(id);
        }
        userRepository.deleteById(id);
    }

    private void ensureEmailFree(String email, Long currentId) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже есть");
            }
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeRole(String role) {
        String value = role.trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Роль: ADMIN, OPERATOR или SENDER");
        }
        return value;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        String value = status.trim().toUpperCase();
        if (!ALLOWED_STATUS.contains(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус: ACTIVE или BLOCKED");
        }
        return value;
    }
}
