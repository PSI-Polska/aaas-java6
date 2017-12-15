# SAMPLE

## How to run
### 1. Start R engine
    docker login registry.gitlab.com/psm2
    docker run --rm -p 6311:6311 registry.gitlab.com/psm2/ts-calculations:0.1

#### Note:
Mentioned docker image configuration can be found [here](https://gitlab.com/psm2/ts-calculations/blob/rengine/calculations-impl/Dockerfile)

### 2. Build project
In `projects` directory:

     mvn clean install

### 3. Run the application
* From your IDE run class `pl.psi.aaas.sample.SimpleTestApp`
* From console, in `projects/sample` directory:

        mvn exec:java

#### Note:
Be aware that sample is configured by default for Docker Toolbox.

This means that application is preconfigured with R container address 192.168.99.100:6311.
