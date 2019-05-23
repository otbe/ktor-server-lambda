# ktor-server-lambda

[![Maven Central](https://img.shields.io/maven-central/v/com.mercateo/ktor-server-lambda.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mercateo%22%20AND%20a:%22ktor-server-lambda-core%22)
[![CircleCI](https://circleci.com/gh/otbe/ktor-server-lambda.svg?style=svg&circle-token=31d37814aa181ae26e97678b105985c58784c23a)](https://circleci.com/gh/otbe/ktor-server-lambda)

## Purpose

`ktor-server-lambda-core` is a proof of concept implementation of an [ktor engine](https://ktor.io/servers/configuration.html) which allows you to run your ktor module in AWS Lambda behind an API Gateway.
Basically its just a mapping of API Gateway events to ktor request/response objects and vice versa.

## Installation

In Maven add our core dependency:  

```xml
<project>
    ...
    <dependencies>
        <dependency>
            <groupId>com.mercateo</groupId>
            <artifactId>ktor-server-lambda-core</artifactId>
            <version>0.0.8</version>
        </dependency>
    </dependencies>
</project>
```

## Usage

Just write your ktor application as always and set the Lambda Handler to `com.mercateo.ktor.server.lambda.LambdaAdapter::handle`. 
After that the Lambda Engine will pickup your application and executes the call pipeline.


Please have a look at our [sample](ktor-server-lambda-sample/) for a compete guide how to use this library. l