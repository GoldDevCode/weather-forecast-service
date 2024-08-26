Weather Forecast Service
========================

This is a simple weather forecast service that provides weather forecast for a given location. The service uses the Met.no API to get the weather forecast data.

The service is implemented using the Spring Boot framework.

The service provides the following endpoints:

/api/v1/forecast/{eventId} - This endpoint provides the weather forecast for the given location. The location can be a city name or a latitude and longitude. The response includes the current weather conditions.

Following are the request parameters for the endpoint:
 - eventId: UUID of the event for which the weather forecast is required.
 - latitude: Latitude of the location for which the weather forecast is required.
 - longitude: Longitude of the location for which the weather forecast is required.
 - startTimeStamp: Start time of the event in UTC for which the weather forecast is required.
 - endTimeStamp: End time of the event in UTC for which the weather forecast is required.

The service performs validation on the request parameters and returns appropriate error messages if the parameters are not valid.

 - The eventId is a required parameter.
 -  latitude and longitude and startTimeStamp and endTimeStamp are required parameters.
 - The latitude and longitude should be valid values.
 - The startTimeStamp and endTimeStamp should be valid timestamps.
 - The endTimeStamp should be greater than the startTimeStamp.
 - Event start time should be in the future upto 7 days, not more than that.

Logic implemented for the service:
 - The service first validates the request parameters.
 - The service then checks if the weather forecast data is already available in the cache.
 - If the weather forecast data is available in the cache, the service returns the response.
 - Cached data is available for 2 hours.
 - The service then calls the Met.no API to get the weather forecast data if cached data is not available.
 - The service then processes the weather forecast data and returns the response.

Processing of data fetched from Met.no API:
 - The service filters the weather forecast data based on the start and end time of the event and also takes into consideration the forecast data upto 7 days.
 - The service then calculates the average temperature for the event duration.
 - The service then returns the response with the current weather conditions.

Average temperature calculation:
 - The service calculates the average temperature for the event duration by taking the average of the temperature values for the event duration. 
 
For eg the start and end time of the event is 2024-08-29T10:00:00Z and 2024-08-29T12:00:00Z, the service calculates the average temperature for this duration.

The service is implemented using the Spring Boot framework and uses the following dependencies:
 - Spring Boot Starter Web
 - Spring Boot Starter Cache
 - Spring Boot Starter Test
 - Redis
 - Lombok
 - JUnit
 - Mockito

To build the service, you can use the following command:

```
mvn clean install
```

To run the service, you can use the following script:

```
chmod +x entrypoint.sh
./entrypoint.sh
```
 It will start the service on port 8080, along with the Redis server on port 6379 and running the tests.
