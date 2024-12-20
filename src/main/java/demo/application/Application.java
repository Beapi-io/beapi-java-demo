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
import io.beapi.api.service.CliService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;

// import jakarta.annotation.Resource;
//import org.quartz.*;
//import io.beapi.api.Scheduler.RateLimiterJob;

//import org.quartz.JobBuilder;
//import org.quartz.JobDetail;
//import org.quartz.Scheduler;
//import org.quartz.SimpleScheduleBuilder;
//import org.quartz.Trigger;
//import org.quartz.TriggerBuilder;
//import org.quartz.impl.StdSchedulerFactory;
//import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableScheduling
@ComponentScan({"${beapi.components}","${application.components}"})
@EnableJpaRepositories(basePackages = {"${beapi.repository}","${application.repository}"})
@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@EnableAsync
class Application implements ApplicationRunner  {

    //@Resource
    //private Scheduler scheduler;

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
/*
        JobDetail job = JobBuilder.newJob(io.beapi.api.Scheduler.RateLimiterJob.class).withIdentity("dummyJobName", "group1").build();

        // Trigger the job
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("dummyTriggerName", "group1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(60).repeatForever()).build();

        // schedule
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        }catch(SchedulerException e){
            System.out.println("test :"+e);
        }
*/
    }

}
