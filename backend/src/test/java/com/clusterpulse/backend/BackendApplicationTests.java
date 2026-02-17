package com.clusterpulse.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
	webEnvironment = WebEnvironment.NONE,
	properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
		"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
		"org.springframework.boot.autoconfigure.jdbc.DataSourceInitializationAutoConfiguration"
	}
)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
