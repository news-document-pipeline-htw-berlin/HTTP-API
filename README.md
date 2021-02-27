# HTTP API

The goal of this API is to make the scraped and analyzed articles and authors, as well as user related functionalities accessible to our frontend and to external requests. It is written with the help of [Akka HTTP](https://doc.akka.io/docs/akka-http/current/index.html), which provides a toolkit to create simple HTTP-based services.

In addition, we use [elastic4s](https://github.com/sksamuel/elastic4s), a Scala client for Elasticsearch. The client is type safe, enabling us to write Elastic-queries programmatically and be notified about errors at compile time.

To access MongoDB directly from within the API, we use the official [MongoDB Scala Driver](https://mongodb.github.io/mongo-scala-driver/).

## Configuration

Configurations for the Webserver, MongoDB and the Elasticsearch connection are stored in `src/main/resources/application.conf`. Information on how the app should be run on production server are located in `inewshttpapi.service`.

## API

### Data

The data classes are stored in `src/main/scala/de/htwBerlin/ai/inews/core` and are used to store Elasticsearch query results and define GET responses. To be able to serialize our objects into JSON we use a custom `JsonFormat` which utilizes functions of `spray-json`.

### JWT

Secured routes may be accessed if the HTTP request contains a Jason Web Token (JWT) in the authorization header. JWTs are generated using the `authentikat-jwt` library. The implementation of this functionality is stored in `src/main/scala/de/htwBerlin/ai/inews/common/JWT`.

### Author, User

The author classes are stored in `src/main/scala/de/htwBerlin/ai/inews/author`, the user classes in `src/main/scala/de/htwBerlin/ai/inews/user`. Both author and user entities in the database are accessed directly by using a connector. These database connectors are implemented with the `mongo-scala-driver` library. `spray-json` is used in both cases for (de-)serialization of documents and objects.

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

#### Analytics

The analytics routes aggregate additional information on the articles, needed to provide analysis functions.

| Method | Route                               | Parameters   | Description |
| :-----:|:------------------------------------| :------------| :---------- |
| `GET`  | `/api/analytics/lemmas`             | -            | Get the 10 most relevant entities for the past 7 days |
| `GET`  | `/api/analytics/terms`              | `query` : `String` <br> `timeFrom` : `Long` <br> `timeTo` : `Long` | Get the occurrences for a term in a specified time frame | 

#### Users

The user routes provide all functionalities associated with user related tasks, e.g. account management and article suggestions. <br>
*Note: routes marked with `AUTH` require a JWT in the request's authorization header.*

| Method | Route                               | Parameters    | Description | Body                 |
| :-----:|:------------------------------------| :------------ | :---------- | :------------------- |
| `DELETE` | `/api/users/account` `AUTH`       | `account` : `true` | Delete active user | { <br> `user`: `String`, <br> `password` : `String` <br> } |
| `DELETE` | `/api/users/account` `AUTH`       | `data` : `true` | Delete keywords for active user | { <br> `user`: `String`, <br> `password` : `String` <br> } |
| `GET`  | `/api/users/account` `AUTH`         | -             | Get data of active user | - |
| `PUT`  | `/api/users/account` `AUTH`         | -             | Update password for active user | { <br> `user`: `String`, <br> `oldPW`: `String`, <br> `newPW`: `String`, <br> `repPW`: `String` <br> } |
| `PUT`  | `/api/users/account` `AUTH`         | -             | Update data for active user | { <br> `_id`: `String`, <br> `username`: `String`, <br> `email`: `String`, <br> `password`: `String`, <br> `suggestions`: `Boolean`, <br> `darkMode`: `Boolean` <br> } |
| `GET`  | `/api/users/keywords` `AUTH`        | -             | Get keyword count of active user | - |
| `PUT`  | `/api/users/suggestions` `AUTH`     | -             | Update keywords of active user | { <br> `list`: `List[String]` <br> } |
| `GET`  | `/api/users/suggestions` `AUTH`     | `offset` : `Int` <br> `count` : `Int` | Get suggested articles for active user | - |
| `POST`  | `/api/users/login`                 | -             | Login with credentials (create JWT) | { <br> `user`: `String`, <br> `password`: `String`, <br> `rememberMe`: `Boolean` <br> } |
| `POST`  | `/api/users/signup`                | -              | Create new account | { <br> `username`: `String`, <br> `email`: `String`, <br> `password`: `String`, <br> `password_rep`: `String` <br> } |

### Authors
The author route provides author information stored in the database. 

| Method | Route                               | Parameters   | Description |
| :-----:|:------------------------------------| :------------| :---------- |
| `GET ` | `/api/authors`                      | `id` : `String` | Get author by id  |

## Elasticsearch queries

All queries and logic needed to return Elasticsearch data is implemented in `src/main/scala/de/htwBerlin/ai/inews/data/ArticleService.scala`, utilizing `elastic4s`. Articles are parsed into Scala classes with the help of the `ArticleHitReader`.

## Running locally

To run the API on your local machine, you first have to connect to the ElasticSearch interface on the iNews-Server via port forwarding. To achieve this, run `ssh -L 9200:localhost:9200 local@news.f4.htw-berlin.de`. The API should now be able to access ElasticSearch.

## Build

To create an executable file bundled with all dependencies we use `sbt-assembly` plugin, which is added in `project/plugins.sbt`.

To build a `.jar` file, run `sbt clean assembly` in the project directory. The generated file will be located in `target/scala<SCALA_VERSION>/inews-backend-assembly-<APP_VERSION>.jar`.

## Deployment

To deploy the app, the `.jar` file as well as the `inewshttpapi.service` have to be copied to the iNews-Server.

Commands to update the `.service` file:
+ Copy file to iNews-server: `scp inewshttpapi.service  local@news.f4.htw-berlin.de:/home/local/inewshttpapi.service`
+ Login at iNews-server: `ssh local@news.f4.htw-berlin.de`
+ Enable sudo access: `su`
+ Move file to correct location: `mv inewshttpapi.service /lib/systemd/system`
+ Restart service: `systemctl restart inewshttpapi.service`

Commands to update the `.jar` file:
+ Copy file to iNews-server: `scp target/scala-2.13/inews-http-api-assembly-0.1.jar local@news.f4.htw-berlin.de:/home/local/inews-http-api-assembly-0.1.jar`
+ Login at iNews-server: `ssh local@news.f4.htw-berlin.de`
+ Enable sudo access: `su`
+ Move file to correct location: `mv inews-http-api-assembly-0.1.jar /home/httpapi/`
+ Restart service: `systemctl restart inewshttpapi.service`

At the time of writing, the API is available within the HTW network at `http://news.f4.htw-berlin.de:8081`. 
