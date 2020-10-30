package com.changgou.system;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BCryptTest {

    @Test
    public void createPwd(){
        String salt = BCrypt.gensalt();
        System.out.println("salt = " + salt);
        String hashpw = BCrypt.hashpw("root", salt);
        System.out.println("hashpw = " + hashpw);
    }
}
