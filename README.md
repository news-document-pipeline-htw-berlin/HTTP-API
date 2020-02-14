# HTTP API

The goal of this API is to make the scraped articles accessible to our frontend as well as external requests. It is written with the help of [Akka HTTP](https://doc.akka.io/docs/akka-http/current/index.html), which provides a toolkit to create simple HTTP-based services.  

In addition we use [elastic4s](https://github.com/sksamuel/elastic4s), a Scala client for Elasticsearch. The client is type safe, enabling us to write Elastic-queries programmatically and be notified about errors at compile time.  

## Configuration

Configurations for the Webserver and the Elasticsearch connection are stored in `src/main/resources/application.conf`. Information on how the app should be run on production server are located in `inewshttpapi.service`. 

## API

### Data 

The data classes are stored in `src/main/scala/de/htwBerlin/ai/inews/core` and are used to store Elasticsearch query results and define GET responses. To be able to serialize our objects into JSON we use a custom `JsonFormat` which utilizes functions of `spray-json`. 

### Available routes

The routes are implemented in `src/main/scala/de/htwBerlin/ai/inews/http/routes`. 

#### Articles

The article routes provides all functionality that is needed to display the scraped data in the frontend.   

| Method | Route                               | Parameters         | Description            |
| :-----:|:------------------------------------| :----------------- | :--------------------- |
| `GET`  | `/api/articles`                     | `offset` : `Int` <br> `count` : `Int` <br> `query` : `String` <br> `department` : `List[String]` <br> `newspaper` : `List[String]` <br> `author` : `String` | Get, search and filter multiple articles |
| `GET`  | `/api/articles/{articleId}`         | -                  | Get one single article by id |
| `GET`  | `/api/articles/authors`             | `query` : `String` | Get and filter authors |
| `GET`  | `/api/articles/departments`         | -                  | Get all departments    |
| `GET`  | `/api/articles/newspapers`          | -                  | Get all newspapers     |

### Analytics

The analytics routes aggregate additional information on the articles, needed to provide analysis functions.

| Method | Route                               | Parameters   | Description |
| :-----:|:------------------------------------| :------------| :---------- |
| `GET`  | `/api/analytics/lemmas`             | -            | Get the 10 most relevant lemmas for the past 7 days |
| `GET`  | `/api/analytics/terms`              | `query` : `String` <br> `timeFrom` : `Long` <br> `timeTo` : `Long` | Get the occurrences for a term in a specified time frame | 

## Elasticsearch queries

All queries and logic needed to return Elasticsearch data is implemented in `src/main/scala/de/htwBerlin/ai/inews/data/ArticleService.scala`, utilizing `elastic4s`. Articles are parsed into Scala classes with the help of the `ArticleHitReader`. 

## Build

To simply create an executable file bundled with all dependencies we use `sbt-assembly` plugin, which is added in `project/plugins.sbt`. 

To build a `.jar` file, run `sbt clean assembly` in the project directory. The generated file will be located in `target/scala<SCALA_VERSION>/inews-backend-assembly-<APP_VERSION>.jar`.

## Deployment

To deploy the app, the `.jar` file as well as the `inewshttpapi.service` have to be copied to the INews-Server. 

Commands to update the `.service` file:
+ Copy file to INews-server: `scp inewshttpapi.service  local@news.f4.htw-berlin.de:/home/local/inewshttpapi.service`
+ Login at INews-server: `ssh local@news.f4.htw-berlin.de`
+ Enable sudo access: `su`
+ Move file to correct location: `mv inewshttpapi.service /lib/systemd/system`
+ Restart service: `systemctl restart inewshttpapi.service`

Commands to update the `.jar` file: 
+ Copy file to INews-server: `scp target/scala-2.13/inews-http-api-assembly-0.1.jar local@news.f4.htw-berlin.de:/home/local/inews-http-api-assembly-0.1.jar`
+ Login at INews-server: `ssh local@news.f4.htw-berlin.de`
+ Enable sudo access: `su`
+ Move file to correct location: `mv inews-http-api-assembly-0.1.jar /home/httpapi/`
+ Restart service: `systemctl restart inewshttpapi.service`

At the time of writing, the API is available within the HTW network at `http://news.f4.htw-berlin.de:8081`. 
