DeadLockDetectService
=========

Daemon service for detect deadlocks

Version
----

0.4


Installation
--------------

```sh
git clone https://github.com/exls/DeadLockDetectService.git
```

Use
----

Put this code in your application
```
    import exls.services.*;

    ...

    //DeadLockDetectService.setSleepTime(1000);
    (new DeadLockDetectService()).start();
```

****
Author
----
Anton Pavlov [anton.pavlov.it@gmail.com](mailto:anton.pavlov.it@gmail.com)
