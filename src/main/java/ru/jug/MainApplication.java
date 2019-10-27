package ru.jug;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class MainApplication {

static {
    ApiContextInitializer.init();
}

public static void main(String[] args) {

    ApiContextInitializer.init();

    try {
        SpringApplication.run(MainApplication.class, args);
    } catch (BeanCreationException e) {
        if (e.toString().contains("Error removing old webhook")) {
            System.out.println("РКН разбушевался, нужен прокси или VPN.");
        }
    }

}

}
