# api-invoker

A java project to invoke Rest API's (See [supported methods](#supported-methods) and capture response in a csv file.

#### supported-methods

- GET
- POST

#### How to build

Project using gradle as a build tool.

````
gradle clean build
````

#### Conventions

##### Api configuration

`application.yml` is used for configuring base rest url's and api to invoke

| Config Name  | Description |
| ------------- | ------------- |
| name  | Api name without spaces. Can be anything. A request file with given name is expected input for `POST` calls. A response file with `<name>-response-<time>.csv` is created after each run.  |
| endpoint | Api endpoint to invoke |
| header | Name of header to pass. Currently supported : `json` |
| httpmethod | HTTP Method. Currently supported: `GET`,`POST` |
| csvHeaders | Input csv file headers for parsing columns |

