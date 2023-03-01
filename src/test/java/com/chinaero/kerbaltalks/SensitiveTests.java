package com.chinaero.kerbaltalks;

import com.chinaero.kerbaltalks.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = KerbaltalksApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "输入：某个作家有一辆车，他喜欢开车，同时有一本小说叫##赌#博#默示录，\n" +
                "    吸烟有害健康，吸#毒和嫖###娼是很危险的事情，\n" +
                "    不要想法子***, aaabb fabccc abc ...\n" +
                "输出：某个作家有一辆车，他喜欢开车，同时有一本小说叫##***默示录，\n" +
                "    吸烟有害健康，***和***是很危险的事情，\n" +
                "    不要想法子***aaabb ***cc ***";
        sensitiveFilter.filter(text);
        System.out.println(text);
    }

}
