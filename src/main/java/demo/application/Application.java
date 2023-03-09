package demo.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.ApplicationArguments;
import demo.application.init.BootStrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.slf4j.LoggerFactory;
import java.util.*;
//import io.beapi.api.service.CliService;


@ComponentScan({"io.beapi.api.*","demo.application.*"})
@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class})
class Application implements ApplicationRunner  {

    @Autowired
    BootStrap bootStrap;

    //@Autowired
    //private CliService cliService;

    @Autowired
    ApplicationContext applicationContext;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(ApplicationArguments args) throws Exception {
        bootStrap.init(applicationContext);
        //cliService.parse();
    }
}
