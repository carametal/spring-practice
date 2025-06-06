package carametal.practice;

import org.springframework.boot.SpringApplication;

public class TestPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.from(PracticeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
