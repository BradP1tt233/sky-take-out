package com.brad.skyserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "sky.jwt.admin-secret-key=test-only-secret-key-at-least-32-bytes-long")
class SkyServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
