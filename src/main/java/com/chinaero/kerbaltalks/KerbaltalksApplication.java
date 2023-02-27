package com.chinaero.kerbaltalks;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class KerbaltalksApplication {

    public static void main(String[] args) {
        SpringApplication.run(KerbaltalksApplication.class, args);
    }

}
