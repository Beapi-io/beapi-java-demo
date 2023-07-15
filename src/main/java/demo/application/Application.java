package demo.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
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
import io.beapi.api.service.CliService;
import org.springframework.beans.factory.annotation.Value;

@ComponentScan({"${beapi.components}","${application.components}"})
@EnableJpaRepositories(basePackages = {"${beapi.repository}","${application.repository}"})
@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class})
class Application implements ApplicationRunner  {

    @Value("${spring.profiles.active}")
    String profile;

    @Autowired
    BootStrap bootStrap;

    @Autowired
    private CliService cliService;

    @Autowired
    ApplicationContext applicationContext;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Running in : "+profile);
        bootStrap.init(applicationContext);
        cliService.parse();

    }
}
