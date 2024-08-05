package translation.lab;

import org.springframework.boot.SpringApplication;

public class TestLabApplication {

    public static void main(String[] args) {
        SpringApplication.from(LabApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
