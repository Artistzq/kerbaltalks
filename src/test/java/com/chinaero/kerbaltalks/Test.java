package com.chinaero.kerbaltalks;

import java.util.UUID;

/**
 * @Author : Artis Yao
 */
public class Test {

    @org.junit.Test
    public void a() {
        System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
    }
}
