
## BOOTSTRAP APPLICATION

Your application requires 'bootstrapping' the starter. The demo project comes with a 'Bootstrap.java' class already created but if you are not using the demo, you will have to create one.

First you will want to import the following classes:

```
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.context.ApplicationContext;

```

...and then after creating a 'init directory with a 'Bootstrap.java' class, **autowire** the class here as well
```
    @Autowired
    BootStrap bootStrap;
```

Then finally, make sure to add the bootstrap to your application runner:
```
    bootStrap.init(applicationContext);
```


