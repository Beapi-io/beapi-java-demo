## CONFIGURATION
Once you have created your project, you will need to edit your '**beapi_api.yaml**' file in your ~/.boot/{env}/ directory and changes the following settings:

## API

- **procCores: (CHANGE)** : Change this to the number of processors your machine has; if running on AWS/Google/SpringCloud, this will be your HyperThreads/vCPU number.

## BOOTSTRAP

- **superUser/login: (CHANGE)** : Superuser that is bootstrapped with your application (also used for functional testing)
- **superUser/password: (CHANGE)** : Superuser that is bootstrapped with your application (also used for functional testing)
- **superUser/email: (CHANGE)** : Superuser that is bootstrapped with your application (also used for functional testing)
- **testUser/login: (CHANGE)** : Testuser that is bootstrapped with your application (also used for functional testing)
- **testUser/password: (CHANGE)** : Testuser that is bootstrapped with your application (also used for functional testing)
- **testUser/email: (CHANGE)** : Testuser that is bootstrapped with your application (also used for functional testing)

