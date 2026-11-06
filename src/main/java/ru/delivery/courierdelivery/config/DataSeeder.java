package ru.delivery.courierdelivery.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.delivery.courierdelivery.domain.Roles;
import ru.delivery.courierdelivery.domain.Statuses;
import ru.delivery.courierdelivery.model.Route;
import ru.delivery.courierdelivery.model.Shipment;
import ru.delivery.courierdelivery.model.Stop;
import ru.delivery.courierdelivery.model.User;
import ru.delivery.courierdelivery.repository.RouteRepository;
import ru.delivery.courierdelivery.repository.ShipmentRepository;
import ru.delivery.courierdelivery.repository.StopRepository;
import ru.delivery.courierdelivery.repository.UserRepository;

@Component
@Profile("!test")
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final StopRepository stopRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            RouteRepository routeRepository,
            ShipmentRepository shipmentRepository,
            StopRepository stopRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.shipmentRepository = shipmentRepository;
        this.stopRepository = stopRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmailIgnoreCase("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setName("Админ");
            admin.setEmail("admin@example.com");
            admin.setRole(Roles.ADMIN);
            admin.setStatus("ACTIVE");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            userRepository.save(admin);
        }
        if (userRepository.findByEmailIgnoreCase("operator@example.com").isEmpty()) {
            User op = new User();
            op.setName("Оператор");
            op.setEmail("operator@example.com");
            op.setRole(Roles.OPERATOR);
            op.setStatus("ACTIVE");
            op.setPasswordHash(passwordEncoder.encode("Operator1"));
            userRepository.save(op);
        }
        if (userRepository.findByEmailIgnoreCase("sender@example.com").isEmpty()) {
            User sender = new User();
            sender.setName("Иван Отправитель");
            sender.setEmail("sender@example.com");
            sender.setRole(Roles.SENDER);
            sender.setStatus("ACTIVE");
            sender.setPasswordHash(passwordEncoder.encode("Sender123"));
            userRepository.save(sender);
        }

        if (routeRepository.count() == 0) {
            User sender = userRepository.findByEmailIgnoreCase("sender@example.com").orElseThrow();
            Route route = new Route();
            route.setRouteDate(LocalDate.of(2026, 10, 20));
            route.setMaxWeight(new BigDecimal("15.000"));
            route.setStatus(Statuses.ROUTE_PLANNED);
            route = routeRepository.save(route);

            Stop s1 = new Stop();
            s1.setRoute(route);
            s1.setAddress("ул. Ленина, 10");
            s1.setSequenceNumber(1);
            stopRepository.save(s1);

            Stop s2 = new Stop();
            s2.setRoute(route);
            s2.setAddress("пр. Мира, 5");
            s2.setSequenceNumber(2);
            stopRepository.save(s2);

            Shipment sh1 = new Shipment();
            sh1.setSender(sender);
            sh1.setAddress("ул. Ленина, 10");
            sh1.setWeight(new BigDecimal("3.500"));
            sh1.setStatus(Statuses.SHIPMENT_CREATED);
            shipmentRepository.save(sh1);

            Shipment sh2 = new Shipment();
            sh2.setSender(sender);
            sh2.setAddress("пр. Мира, 5");
            sh2.setWeight(new BigDecimal("2.000"));
            sh2.setStatus(Statuses.SHIPMENT_CREATED);
            shipmentRepository.save(sh2);

            Shipment sh3 = new Shipment();
            sh3.setSender(sender);
            sh3.setAddress("ул. Садовая, 3");
            sh3.setWeight(new BigDecimal("12.000"));
            sh3.setStatus(Statuses.SHIPMENT_CREATED);
            shipmentRepository.save(sh3);
        }
    }
}
