# Introduction to Algorithm As A Service (AAAS)

Table of Contents
- [Introduction to Algorithm As A Service (AAAS)](#introduction-to-algorithm-as-a-service-aaas)
    - [Context of usage and Containers](#context-of-usage-and-containers)
        - [1. Context of 3AS usages](#1-context-of-3as-usages)
            - [1.1 Context of 3AS in PSImarket](#11-context-of-3as-in-psimarket)
        - [2. Containers](#2-containers)
            - [2.1 EDM/PSImarket Containers](#21-edmpsimarket-containers)
        - [3. EDM Components](#3-edm-components)
    - [Use cases](#use-cases)
        - [1. The most general idea](#1-the-most-general-idea)
        - [1.1 EDM general idea](#11-edm-general-idea)

## Context of usage and Containers

### 1. Context of 3AS usages

The most basic Community AIORAE needs:

* use already existing artificial inteligence, data engeneering tools and algorithms. This means using as "The engine": python, R, mathematica, matlab, julia to name a few.
* TODO business needs
* it should be simple to add to the product
* simple to set up (3AS, the engine, monitoring, scaling etc)

![](4c/AI%20OR%20AE%20Context.png)

#### 1.1 Context of 3AS in PSImarket

PSImarket needs:

* solve forecasting problems - Time Series based problems
* use R as engine implementation
* provide means for business analysts to work with production data in RStudio
* it should take very limited effor to introduce R to PSImarket

![](4c/EDM%20PSImarket%20Context.png)


### 2. Containers

This point mostly opens questions:

* where does the date come from (time series, images, sounds, parametrization)?
  * from product?
  * product data pushed to auxiliary engine database accesible direcltly from the entine?
  * implementation of data access layer in the engine?
* how communication is done?
  * sync/async
  * are the results stored on some intermediate database?
  * what is the deployment strategy?
* where is the source/binary evaluated by the engine
  * how it is defined? on what data? what API - will it be the same as production?
  * how it is provided to the engine? statically? dynamically?
* the same question applies to trained models
* address "openshift issue" (and cloud)?

![](4c/AI%20OR%20AE%20Containers.png)

#### 2.1 EDM/PSImarket Containers

3AS implementation for EDM answers to those questions:

* Data (Time Series) is provided by PSImarket and passed through 3AS to R. Parametrization is passed with calculation request.
* Communication is done synchronously with method calls - library is embedded.
* Scripts are available for both, RStudio and R through Git repository
* Use case from Rstudio works exaclty the same way as Use case from product
* Forecasting models are stored on ModelsRepository to be available in the future.
* Engines, Scripts repository are dockerized, ready to be run on k8s or openshift

![](4c/EDM%20PSImarket%20Containers.png)

### 3. EDM Components

## Use cases

Below diagrams describe different use cases on sequence diagrams gradually introducing new components.

### 1. The most general idea

The simplest and most basic form of our requirements is to call some prepared definition on the Engine through gateway/bridge component (thus integration with PSIproduct consumes small amount of effort). Such a general idea can be turn into many particular use cases involving single call of external computational engine, e.g.:
* forecasting/predicting with already prepared forecasting model,
* categorizing the data with respect to trained and available Machine Learning model,
* an application of a unsupervised Machine Learning on provided data.

![](useCases/General%20idea.png)

### 1.1 EDM general idea

EDM/PSI market use case is Time Series forecasting using R.

R Scripts are developed and pushed to Scripts repository and synchronized with the REngine through ScriptsSynchronizer.

When execution of R script is requested, first scripts synchronization status is checked. Then, according to the definition of time series based calculation, the time series are fetched from PSImarket implementation of TimeSeries Repository and mapped to script variables.

Next step is calling the prognosis script on the engine. When this is finished, time series are returned by REngine and saved in PSImarket's database using TimeSeriesRepository.
After the results are stored in database, PSImarket is informed about result of calculation (error or ok).
<!-- TODO add Models Repositoty -->

![](useCases/AKT-1238/Call%20Script%20when%20synchronization%20is%20not%20running.png)