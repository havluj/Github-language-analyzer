# Language Analyzer

This is a take-home assignment for a Productboard interview.

## Assignment

Write an application providing an API endpoint for fetching the percentage of each language represented in Producboard’s GitHub organization public repositories. The application should fetch the data daily and persist them. The time when the data gets fetched is arbitrary, and you may use whichever persistence storage.

GitHub provides REST and GraphQL APIs which are both well documented. You can choose what suits you the most to gather whatever data necessary.

> You shouldn’t need to use any authorization token to access the required information for the task if you choose REST API (just be aware of the rate limit for unauthenticated requests).
> If you choose GraphQL API you’ll have to create and use access token to be able to query required information.

The response should have the following format in JSON:

````
{
    "Ruby": 0.5,
    "TypeScript": 0.2,
    "Python": 0.3
}
````

Code can be in Kotlin or Java and you can use build tool of your choice. We encourage you to use Spring framework.

You should provide tests and instructions on how to run your application. The deliverable should be a repository on GitHub containing source code for the application.

You can find Productboard’s GitHub organisation [here](https://github.com/productboard).

## How to run

Before using the app, there are 2 prerequisites:

1. Update `db.location` in `src/main/resources/application.properties`. This property configures where the app persists collected statistics.
2. Update `github.token` in `src/main/resources/application.properties`. [Here](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) is how you get one.

To run the server, you have 2 options:

1. Run the application with Gradle: `./gradlew bootRun`.
2. Build a JAR: `./gradlew build` and then run the JAR file `java -jar build/libs/languageanalyzer-1.0.jar`.

### Usage

To get Productboard's GitHub language statistics:

````
curl http://localhost:8080/org/productboard/languages
````

To see a "failure" response, try providing another org:

````
curl http://localhost:8080/org/invalid/languages
````

### Unit tests

*To run all unit tests, run `./gradlew test`.*

100% class coverage, 100% method coverage (excluding main), 99% line coverage.

## Notes

Here are some notes and answers to questions I am expecting to be asked:

- The service only exposes a single HTTP API. No HTTPS. I didn't want to deal with certificates for the purposes of this exercise.
- In the real-world, I would want to have a more robust logging configuration (e.g., the ability to change what and how we log based on stage).
- In the real-world, I would want to be emitting operational health metrics (application, JVM, and service metrics).
- I didn't bother setting up a formalized codestyle, which I normally like to have as part of my projects. I also like to run checkstyle as part of the build process. 
- I didn't write integration tests because of time.
- I didn't properly test if the app is going to have to manually manage its file database (there are options to rebuild it -- to reclaim space). I would want to do this for a production app.
- I would probably not use the same built-in database if I was building a production-ready system.
- And finally, I would not choose to build an app like this with Spring. If possible, I'd likely go server-less. High-level design: have a lambda function run every day, and let it compute org's language statistics. Those would be saved into a document db (e.g., DynamoDB), where the key is the org's name and the value is a JSON of stats (desired result). Then we could only have something like an ApiGateway, which would be connected directly to the DDB (and therefore needing no application code to present the stats). We might need another Lambda function behind the ApiGateway, if we'd want more fancy logic -- like to compute stats on-demand when they are not pre-computed and stored in DDB.  

I also made some assumptions:

- That we're ok with rounding errors. The percentage total can therefore be little above or below 100%. If we wanted a more robust solution, we could do that by rounding later and making sure we always get to 100%. We could also let users specify precision.
- I don't deal with locales properly (we might want to return different decimal points -- e.g., "," vs ".").
- I assume enough disk space and enough heap memory to store the collected language stats. This is a safe assumption, since I'm only allowing Productboard's stats.

