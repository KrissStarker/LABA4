package ru.delivery.courierdelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@Import(TestSecurityConfig.class)
class CourierDeliveryApplicationTests {

	@Test
	void contextLoads() {
	}

}
