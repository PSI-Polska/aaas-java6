# Introduction to Algorithm As A Service (AAAS)

<!-- TOC -->

- [Introduction to Algorithm As A Service (AAAS)](#introduction-to-algorithm-as-a-service-aaas)
    - [Context of usage and Containers](#context-of-usage-and-containers)
        - [1. Context of AaaS usages](#1-context-of-aaas-usages)
            - [1.1 Context of AaaS in PSImarket](#11-context-of-aaas-in-psimarket)
        - [2. Containers](#2-containers)
            - [2.1 EDM/PSImarket Containers](#21-edmpsimarket-containers)
        - [3. EDM Components](#3-edm-components)
    - [Use cases](#use-cases)
        - [1. The most general idea](#1-the-most-general-idea)
        - [1.1 The algorithm call with asynchronous communication](#11-the-algorithm-call-with-asynchronous-communication)
        - [1.2 The algorithm call with synchronous communication](#12-the-algorithm-call-with-synchronous-communication)
        - [1.3 The algorithm does not require additional data](#13-the-algorithm-does-not-require-additional-data)
        - [1.4 The required data is passed together with calculation definition](#14-the-required-data-is-passed-together-with-calculation-definition)
        - [1.5 The required data is fetched from the data base](#15-the-required-data-is-fetched-from-the-data-base)
        - [1.6 Mixed scenario (with auxuliary data base)](#16-mixed-scenario-with-auxuliary-data-base)
        - [1.7 Sequential run on computational engines](#17-sequential-run-on-computational-engines)
        - [1.8 Sequential multiple run on computational engines](#18-sequential-multiple-run-on-computational-engines)
        - [1.2 EDM (AKT-1238) use case](#12-edm-akt-1238-use-case)

<!-- /TOC -->


## Context of usage and Containers

### 1. Context of AaaS usages

The most basic Community AIORAE needs:

* use already existing artificial inteligence, data engeneering tools and algorithms. This means using as "The engine": python, R, mathematica, matlab, julia to name a few.
* TODO business needs
* it should be simple to add to the product
* simple to set up (AaaS, the engine, monitoring, scaling etc)

![](4c/AI%20OR%20AE%20Context.png)

#### 1.1 Context of AaaS in PSImarket

PSImarket needs:

* solve forecasting problems - Time Series based problems
* use R as engine implementation
* provide means for business analysts to work with production data in RStudio
* it should take very limited effor to introduce R to PSImarket

![](4c/EDM%20PSImarket%20Context.png)


### 2. Containers

This point mostly opens questions:

* where does the data come from (time series, images, sounds, parametrization)?
  * from product?
  * product data pushed to auxiliary engine database accesible direcltly from the engine?
  * implementation of data access layer in the engine?
* how communication is done?
  * sync/async
  * are the results stored on some intermediate database?
  * what is the deployment strategy?
* where is the source/binary evaluated by the engine
  * how it is defined? on what data? what API - will it be the same as production?
  * how it is provided to the engine? statically? dynamically?
* the same question applies to trained models
* how can we address "openshift issue" (and cloud)?

![](4c/AI%20OR%20AE%20Containers.png)

#### 2.1 EDM/PSImarket Containers

AaaS implementation for EDM answers to those questions:

* Data (Time Series) is provided by PSImarket and passed through AaaS to R. Parametrization is passed with calculation request.
* Communication is done synchronously with method calls - library is embedded.
* Scripts are available for both, RStudio and R through Git repository. PSImarket uses the scripts indirectly through R.
* Use case from Rstudio works exaclty the same way as Use case from product
* Forecasting models are stored on ModelsRepository to be available in future predictions.
* Engines, Scripts repository are dockerized, ready to be run on k8s or openshift

![](4c/EDM%20PSImarket%20Containers.png)

### 3. EDM Components

## Use cases

Below diagrams describe different technical use cases on sequence diagrams gradually introducing new components.
Technical idea is to provide three components:

* AaaS library - responsible for providing API to the product, handling the use cases and executing them on the enginesm
* Preconfigured engines - engine configuration ready to work with AaaS,
* Integration layer with PSI products - standard for PSI product ways of communication with AaaS with API ready for new integration scenarios.

### 1. The most general idea

The simplest and most basic form of our requirements is to call some prepared definition on the Engine through gateway/bridge component (thus integration with PSIproduct consumes small amount of effort). Such a general idea can be turn into many particular use cases involving single call of external computational engine, e.g.:

* forecasting/predicting with already prepared forecasting model,
* categorizing the data with respect to trained and available Machine Learning model,
* an application of a unsupervised Machine Learning on provided data.

![](useCases/General%20idea.png)

### 1.1 The algorithm call with asynchronous communication

This kind of architecture is more precise description of general sketch, shown in previous section. The process is asynchronous from the point of view of business user. An additional gateway has been introduced in order to coordinate the flow of computational process. In particular one can imagine an instantiation of multiple computational engines and passing the calculation request to chosen one or multiple ones, the latter in the case of parallel execution.

![](useCases/Asynchronous%20communication.png)

### 1.2 The algorithm call with synchronous communication

Similar to the previous one, but the communication is synchronous.

![](useCases/Synchronous%20communication.png)


### 1.3 The algorithm does not require additional data

The scenario where no additional data is required for the calculation

![](useCases/Definition%20does%20not%20require%20additional%20data.png)

### 1.4 The required data is passed together with calculation definition 

Probably one of the most often scenario. The responsibility of calling application is to provide both the calculation definition and the accompanied data. For instance, one can imagine the forecasting scenario for given period of time (which is a part of calculation definition) where a vector of predictors is also expected (e.g. the forecasted weather conditions time series).  

![](useCases/Definition%20is%20passed%20with%20data.png)

### 1.5 The required data is fetched from the data base

Scenario where the responsibility of retrieving the data needed by an algorithm is passed to the Gateway. The Gatewey is further passing the data together with the calculation definition to chosen computational engine.

![](useCases/Definition%20called%20with%20data%20source%20definition.png)

### 1.6 Mixed scenario (with auxuliary data base)

Here an additional, auxiliary data base is involved. This data base stores the result of the calculation, which can be further accessed from the application. 

![](useCases/Definition%20of%20mixed%20scenario.png)

### 1.7 Sequential run on computational engines

The calculation can be of multistep nature, there fore it can be realised by multiple computational engines. In the example below one can see the R calculation followed by Python execution. In particular the R computational instance can be involved in multicriteria optimization which results in a family of solution forming a Pareto front. The Python computational engine incorporates the user preferences and choses one of Pareto-optimal solutions which is most relevant from the point of view of user preferences. 

![](useCases/Sequential%20Run%20on%20R%20and%20Python%20computational%20engines.png)

### 1.8 Sequential multiple run on computational engines

Similar to the previous scenarion, there is an additional step carried out within the Python computational engine.

![](useCases/Sequential%20Run%20on%20R%20and%20Python%20computational%20engines%20(multiple%20calculations).png)


### 1.2 EDM (AKT-1238) use case

EDM/PSImarket use case is Time Series forecasting using R.

R Scripts are developed and pushed to Scripts repository and synchronized with the REngine through ScriptsSynchronizer.

When execution of R script is requested, first scripts synchronization status is checked. Then, according to the definition of time series based calculation, the time series are fetched from PSImarket implementation of TimeSeries Repository and mapped to script variables.

Next step is calling the prognosis script on the engine. When this is finished, time series are returned by REngine and saved in PSImarket's database using TimeSeriesRepository.
After the results are stored in database, PSImarket is informed about result of calculation (error or ok).
<!-- TODO add Models Repositoty -->

![](useCases/AKT-1238/Call%20forecasting%20script%20when%20synchronization%20is%20not%20running.png)
