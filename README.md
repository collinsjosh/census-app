# census-app

This is a personal project designed to explore several technologies and to demonstrate an end to end application.

You can [see the application](https://peaceful-taiga-33169.herokuapp.com/index.html) running on Heroku.

## Getting Started

From your terminal:

```bash
> git clone https://github.com/collinsjosh/census-app.git
> cd census-app
> lein run
```

Go to [localhost:8080/index.html](http://localhost:8080/index.html) and your discover the secrets of US demographics.

## About
Census-app uses the [Quick Facts](https://www.census.gov/quickfacts/table/PST045215/00) data that summarizes the 2010 U.S. census results.  The app presents a [population pyrimad](https://en.wikipedia.org/wiki/Population_pyramid) for and state and county in the U.S.

## How it works
census-app is built with:
1. A SQL Server database hosted on the Azure platform
2. An API built with Clojure using the Pedestal library
3. A single page application that uses the Highcharts javascript library





