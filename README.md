# town-visit

Example usage of scala Play, slick and AKKA streams in REST API. 

This projects implement simple REST service to calculate traveled distance between cities.
Each travel entry is stored in the database and user can call REST endpoint to find 
shortest traveled path in kilometers or days between visited cities.

## Prerequisites

1) Setup JDK and SBT
2) Install postgres and setup user with database
3) Create config file `app/config/application.conf` with DB credentials as following:
```
db.default {
  url = "jdbc:postgresql://127.0.0.1/mydb"
  username = "myuser"
  password = "mypassword"
}
```

## Usage

Run server on local:
```
sbt run
```

By default server will be started on:
```
http://localhost:9000
```

### Travels file

Before firs tun import TSV file with travels. File needs to contain lines with 
three columns separated by TAB character. Header is optional.

File syntax is similar to following:

#### File Format
* Date column - Time when city was visited in format "yyyy-mm-dd"
* City column - Name of the city
* Kilometers column - Traveled distance from last city, integer


#### Examples

```
Date	City	Kilometers
2019-05-01	Praha	0
2019-05-05	Beroun	32
2019-05-06	Praha	32
```

See [examples](./examples) folder with example files.

#### Travels generator

There is simple tool to generate example TSV files. City names are generated by 
randomly selected characters [A-Z]. Distance between cities is given from 
distance between each character in the set. There will be 24 visit in each day 
and visits are uniformly distributed.

It can be run from commandline using SBT ass following:

```
sbt "runMain utils.TravelsGenerator <file name> <file size> <name size>"
```

Parameters:
* File name - path to generated file, any existing file will be replaced
* File size - number of visits in file
* Name size - number of characters in randomly generated city name

Following command will generate 1000 visits with name like `AA`, `AD`, `QG`.

```
sbt "runMain utils.TravelsGenerator generated-travels.tsv 1000 2"
```

Tweak number of characters with file size. Too small number of characters 
on big file will result in dataset where all combinations of cities was visited 
consequently. Too big number of characters will create very small subsets.

### API

#### Version

* GET `/api/version`

Returns information about currently running application and build version.

```
curl http://localhost:9000/api/version
```
```json
{
   "name":"town-visit",
   "version":"1.0-SNAPSHOT",
   "scalaVersion":"2.12.8",
   "sbtVersion":"1.2.8",
   "builtAtString":"2019-06-10 15:58:30.157",
   "builtAtMillis":1560182310157,
   "git":{
      "commit":"c6d67e69cb5efbf9e7d93c473a3f4741b2515d67",
      "branch":"master"
   }
}
```

#### Importing travels file

* POST `/api/traveled/import`

Imports TSV file sent in raw body. Streams content of the file into a database
and triggers recalculation of traveled index column.

```
curl -X POST  --data-binary "@examples/travel.tsv" http://localhost:9000/api/traveled/import
```

#### Calculate minimal distance

* GET `/api/traveled/traveled/distance?from=A&to=B`

Calculate smallest travelled distance between two given cities. Parameters 
are mandatory and case insensitive.

```
curl "http://localhost:9000/api/traveled/distance?from=praha&to=brno"
```
```json
{
  "pathExists": true,
  "kilometers": 360
}
```

```
curl "http://localhost:9000/api/traveled/distance?from=olomouc&to=praha"
```
```json
{
  "pathExists": false
}
```

#### Calculate minimal duration

* GET `/api/traveled/traveled/duration?from=A&to=B`

Calculate smallest travelled duration in days between two given cities. Parameters 
are mandatory and case insensitive.

```
curl "http://localhost:9000/api/traveled/duration?from=praha&to=brno"
```
```json
{
  "pathExists": true,
  "days": 2
}
```

```
curl "http://localhost:9000/api/traveled/duration?from=olomouc&to=praha"
```
```json
{
  "pathExists": false
}
```

#### Daily travels chart

* GET `/api/traveled/chart/daily`

Gets traveled distance in kilometers for each day. 

```
curl "http://localhost:9000/api/traveled/chart/daily"
```
```json
{
   "items":[
      {
         "date":"2019-05-01",
         "traveled":0
      },
      {
         "date":"2019-05-05",
         "traveled":32
      },
      {
         "date":"2019-05-06",
         "traveled":96
      },
      {
         "date":"2019-05-15",
         "traveled":540
      },
      {
         "date":"2019-05-16",
         "traveled":200
      },
      {
         "date":"2019-05-17",
         "traveled":280
      },
      {
         "date":"2019-05-18",
         "traveled":80
      }
   ]
}
```

#### Weekly travels chart

* GET `/api/traveled/chart/weekly`

Gets traveled distance in kilometers for each week in year. 

```
curl "http://localhost:9000/api/traveled/chart/weekly"
```
```json
{
   "items":[
      {
         "year":2019,
         "week":18,
         "traveled":0
      },
      {
         "year":2019,
         "week":19,
         "traveled":128
      },
      {
         "year":2019,
         "week":20,
         "traveled":1100
      }
   ]
}
```

## Testing

Application unit tests can be run using SBT:

```
sbt test
```

Tests are using in memory H2 database to test DB queries.

## Known problems

### DB import

1) For large datasets (more than 10M rows) takes long time to import. Import 
re-calculates distances from beginning and there is cartesian product in 
UPDATE statement. It would be better to import files and then do calculation 
in memory.

2) Only one import can run at a time. Concurrent imports will affect each other.


### Testing H2 database

1) There is shared H2 instance between tests. Each tests needs to clear database
otherwise tests can affect each other.

2) DB migrations are run for all test applications. This could become slow when
more tests are in place.

3) H2 can't emulate all postgres features. Re calculating data couldn't be tested 
using H2.

