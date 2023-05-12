
# Pie Cloud Project

Pie Cloud Project was started as part of learning new features of Spring Framework with ['Spring in Action, 5th Edition'](https://github.com/dfparker2002/books-2/blob/master/Spring%20in%20Action,%205th%20Edition.pdf) book, but was continued and ended as full-fledged pet project.

## Overview
Pie Cloud is the **RESTful API** for café, which main features are pies and more pies that customer can order with ingredients they want. The application exposes the opportunity to create order with pies and additions and handle these orders by cooks.
## Key features
* Application was built with [**Spring Boot 2**](https://spring.io/) and [**Project Reactor**](https://projectreactor.io/). 
* [**MongoDB**](https://www.mongodb.com/) was selected as the main database, combined with MongoDB database references and  `ReactiveMongoRepository` interfaces.
* In order to implement **SSE**, there was used [**Apache Kafka**](https://kafka.apache.org/) message broker.
* API gives opportunity to **upload, store and sent images** for additions, ingredients and pies. Images are saved in separate MongoDB collection.

## Requirements
Pie CLoud requires the following to run:
* [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
* [Dcoker](https://www.docker.com/)
## Installation and Running

Install Pie Cloud with `git clone`, `gradle` and `docker`

```bash
$ git clone https://github.com/stas-bukovskiy/pie-cloud.git
$ cd pie-cloud
$ gradle bootJar 
$ docker build .
$ docker-compose up -d
```
    
## Usage
Url to see Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
And you will see something looks like:
![Swagger Ui Example](swagger-ui-example.png)

