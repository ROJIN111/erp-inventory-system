package com.example.erpinventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.erpinventory.mapper")
public class ErpInventorySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpInventorySystemApplication.class, args);
    }

}
