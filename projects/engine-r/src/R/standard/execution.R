# Title     : Execution layer for the R forecasting scripts
# Objective : TODO
# Created by: rbachorz
# Created on: 2018-01-19

wdir="E:\\R\\EDM\\aaas\\projects\\engine-r\\src\\R\\standard"
setwd(wdir)

source("prepareData.R")

dfData <- readData("E:\\R\\EDM\\aaas\\projects\\engine-r\\src\\R\\standard\\Load_Temp_TimeSeries_PPL_Date_WorkingDay_Load_Temperature.csv")

# prepare the data frame with parameterization 
dfParameters <- data.frame(name = as.character(), value = as.character(), stringsAsFactors = FALSE)
colnames(dfParameters) <- c("name", "value")
##########
##########
# training
# train the model
# pathModelOut - location of the model file
# trainBeg - beginning of train period
# trainEnd - end of train period
# resolution - time resoluition when creating the sequence
dfParameters[1,] <- c("pathModelOut", paste(wdir, "\\svmModel.rds", sep = ""))
dfParameters[2,] <- c("trainBeg", as.character(format(min(dfData$Date), "%Y-%m-%dT%H:%M")))
dfParameters[3,] <- c("trainEnd", as.character(format(max(dfData$Date), "%Y-%m-%dT%H:%M")))
dfParameters[4,] <- c("resolution", "hour")

dfData$Date <- NULL
source("trainSVM.R")
trainingResult <- run(dfData = dfData, dfParameters = dfParameters)

##########
##########
# forecasting
# train the model
forecastBeg <- as.POSIXct("2015-03-01 00:00", tz = "GMT")
forecastEnd <- as.POSIXct("2015-03-08 00:00", tz = "GMT")

# prepare the data frame with parameterization 
dfParameters <- data.frame(name = as.character(), value = as.character(), stringsAsFactors = FALSE)
colnames(dfParameters) <- c("name", "value") 
  
dfParameters[1,] <- c("forecastBeg", as.character(format(forecastBeg, "%Y-%m-%dT%H:%M")))
dfParameters[2,] <- c("forecastEnd", as.character(format(forecastEnd, "%Y-%m-%dT%H:%M")))
dfParameters[3,] <- c("resolution", "hour")
dfParameters[4,] <- c("pathModelIn", paste(wdir, "\\svmModel.rds", sep = ""))

dfData <- readData("E:\\R\\EDM\\aaas\\projects\\engine-r\\src\\R\\standard\\Load_Temp_TimeSeries_PPL_Date_WorkingDay_Load_Temperature.csv")
dfPredictors <- dfData %>% filter(Date > forecastBeg, Date <= forecastEnd) %>% select(Temperature, WorkingDay)

source("forecastSVM.R")
forecastResult <- run(dfData = dfPredictors, dfParameters = dfParameters)

