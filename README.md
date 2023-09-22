# Bus Route Calculator

The objective of this application is to provide a REST API that retrieves information about the top 10 SL bus lines with the highest number of stops, including details about all the stops on those lines. This functionality is accomplished by utilizing the Trafiklab's open API, which can be accessed at http://www.trafiklab.se/api/sl-hallplatser-och-linjer-2.


## Trafiklab's API Key

### Why do you need TrafikLab's API Key

This application relies on the Trafiklab's REST API, which requires an API key for access. The only available plan for the Trafiklab API is the bronze plan, offering 5 requests per minute and 500 requests per month.

To ensure scalability and flexibility, it is more practical to have consumers provide their own API keys when using the application. Therefore, a query parameter has been incorporated into the HTTP request, allowing consumers to pass their own Trafiklab API Key, which will then be utilized for the necessary Trafiklab API calls.

### Get the APi key

Follow the below step to get a new trafiklab's API key
 * Step 1: Log in to https://developer.trafiklab.se/login. Register if you dont have an account
 * Step 2: Create a new project
 * Step 3: Add a new API key to the newly created project and select 'SL Hållplatser och Linjer 2' as 'api'
 * Step 4: Save the newly generated API Key

## Run locally

### Clone the repositiry to local machine
```
git clone https://github.com/Lagnashree/bus-route-calculator.git
```

### run it with either of the command below (docker compose)

```
cd bus-route-calculator /
docker-compose up --build
```


OR         


```
cd bus-route-calculator /
docker compose up --build
```

### Test the HTTP endpoint with curl. 

Replace "APIKEY" with a valid Trafiklab API Key and run it in the terminal.

```
curl --location --request GET 'http://localhost:8080/api/v1/busline?apiKey=<APIKEY>'
```