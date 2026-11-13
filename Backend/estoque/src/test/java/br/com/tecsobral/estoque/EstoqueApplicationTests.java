package br.com.tecsobral.estoque;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:estoque-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none"
})
class EstoqueApplicationTests {

	@Test
	void contextLoads() {
	}

}
