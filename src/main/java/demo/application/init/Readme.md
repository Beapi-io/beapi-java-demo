
## BOOTSTRAP

Bootstrap requires the '*BootstrapService*' from the starter. Simply import the class and instantiate it:

```
import io.beapi.api.service.BootstrapService;

...

    @Autowired
    BootstrapService bootstrapService;

```

...and then make sure to reference the **bootstrapService.initAll()** in your **init** method:

```
    public void init(ApplicationContext applicationContext) {
        bootstrapService.bootstrapAll();
    }
```


