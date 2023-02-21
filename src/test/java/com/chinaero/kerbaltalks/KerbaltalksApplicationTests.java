package com.chinaero.kerbaltalks;

import com.chinaero.kerbaltalks.dao.AlphaDao;
import com.chinaero.kerbaltalks.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = KerbaltalksApplication.class)
class KerbaltalksApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);

        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.printf(alphaDao.select());

        alphaDao = applicationContext.getBean("alphaDaoHibernateImpl", AlphaDao.class);
        System.out.printf(alphaDao.select());
    }

    @Test
    public void testBeanManagement() {
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);

        System.out.println(alphaService);
    }

    @Test
    public void testBeanConfig() {
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired
    @Qualifier("alphaDaoHibernateImpl")
    private AlphaDao alphaDao;

    @Test
    public void testDI() {
        System.out.println(alphaDao.select());
        System.out.println(applicationContext.getBean(AlphaDao.class).select());
    }
}
