readData <- function(filePath){
  library(dplyr)
  library(data.table)
  
  energyData <- fread(filePath, skip = 0, header = TRUE, stringsAsFactors = FALSE, sep = ";")
  energyData$Date <- as.POSIXct(energyData$Date, format = "%Y-%m-%d %H:%M", tz = "GMT")
  energyData$WorkingDay <- as.factor(energyData$WorkingDay)
  
  return(energyData)
  #return(0)
}