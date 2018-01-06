# Introduction to Algorithm As A Service (AAAS)

## Context of 3AS usage

The most basic Community AIORAE needs:

* use already existing artificial inteligence, data engeneering tools and algorithms. This means using as "The engine": python, R, mathematica, matlab, julia to name a few.
* TODO business needs
* it should be simple to add to the product
* simple to set up (3AS, the engine, monitoring, scaling etc)

![](4c/AI%20OR%20AE%20Context.png)

### Context of 3AS in PSImarket

PSImarket needs:

* solve forecasting problems - Time Series based problems
* use R as engine implementation
* provide means for business analysts to work with production data in RStudio
* it should take very limited effor to introduce R to PSImarket

![](4c/PSImarket%20Context.png)


## Containers

This point mostly opens questions:

* where comes the data (time series, images, sounds, parametrization) from?
  * from product?
  * product data pushed to auxiliary engine database accesible direcltly from the entine?
  * implementation of data access layer in the engine?
* how communication is done?
  * sync/async
  * are the results stored on some intermediate database?
* where is the source/binary evaluated by the engine
  * how it is defined? on what data? what API - will it be the same as production?
  * how it is provided to the engine? statically? dynamically?
* the same question applies to trained models
* address "openshift issue" (and cloud)?

![](4c/AI%20OR%20AE%20Containers.png)