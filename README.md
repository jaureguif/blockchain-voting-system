Blockchain Asset Tracking System
================================

[![Coverage](https://codecov.io/gh/jaureguif/blockchain-voting-system/branch/master/graph/badge.svg)](https://codecov.io/gh/jaureguif/blockchain-voting-system)
[![Build Status](https://travis-ci.org/jaureguif/blockchain-voting-system.svg?branch=master)](https://travis-ci.org/jaureguif/blockchain-voting-system)

## Prerequisites
----------------
To build this project, following dependencies must be met
  * JDK 1.8 or above
  * Apache Maven
  * Docker - v1.12 or higher
  * Docker Compose - v1.8 or higher 

## Run tests
------------

Current tests:
* src/test/java - two unit test source files.
* src/test/fixture: artifacts required for test.

```sh
$ cd src/test/fixture/sdkintegration
$ ./fabric.sh clean
$ docker-compose up -d
$ cd ../../../../
$ mvn package
```

## Stop services
----------------

```sh
$ cd src/test/fixture/sdkintegration
$ docker-compose down
```
