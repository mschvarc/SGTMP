# Smart Grid Testing Management Platform


The Smart Grid Testing Management Platform is a platform for executing real-time hardware in the loop testing. It is focused primarily on the Smart Grid domain; however, it can be also used in other domains requiring the simulation and integration of Multi Agent Systems. The platform is based on the [Mosaik co-simulation framework](https://mosaik.offis.de/), which is used to provide simulation capabilities. The result is a platform for testing of Smart Grids, which allows the users to define a Smart Grid topology, test pass criteria and integrate both hardware and software simulators into a testing environment. The platform connects all of the required components into a user-defined Smart Grid for each test. Simulator integration is accomplished by using a Python or Java wrapper for communication. The test results and step-by-step simulation data points can be examined to determine the cause of a test failure. Non-conflicting tests can be executed concurrently. 

This project is based on the [Masaryk University Bachelor's Thesis of Martin Schvarcbacher](https://is.muni.cz/th/nbyn6/).


## Platform Setup

```
$ docker build .
...
Successfully built {container id}
$ docker run -p 8080:8080 -it {container id}
```


## License
GNU Lesser General Public License
Version 2.1, February 1999


