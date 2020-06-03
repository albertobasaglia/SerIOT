# Reading real world data.

The arduino software uses some sensors to read data from the real world. This data is sent through a TCP socket to a java server whose only taks is to save the data on a mongoDB server. The mongoDB atlas provides an endpoint which returns the data in json format. This json data is displayed with some chartsin the Angular web app.


