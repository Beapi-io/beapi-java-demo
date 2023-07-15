package demo.application.init;

import io.beapi.api.service.BootstrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;

@Component
public class BootStrap {

    @Autowired
    BootstrapService bootstrapService;

    public void init(ApplicationContext applicationContext) {
        bootstrapService.bootstrapAll();
    }

}
