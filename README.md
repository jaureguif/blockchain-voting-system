Blockchain Asset Tracking System
================================

[![Coverage](https://codecov.io/gh/jaureguif/blockchain-voting-system/branch/dev/graph/badge.svg)](https://codecov.io/gh/jaureguif/blockchain-voting-system)
[![Build Status](https://travis-ci.org/jaureguif/blockchain-voting-system.svg?branch=dev)](https://travis-ci.org/jaureguif/blockchain-voting-system)

## Prerequisites

To build this project, following dependencies must be met:
  * JDK - v1.8+
  * Apache Maven - v3.3+
  * Docker - v1.12+
  * Docker Compose - v1.8+

## Run tests and start blockchain from scratch

```sh
$ cd src/test/fixture/sdkintegration
$ ./fabric.sh clean  # Drops all docker images!
$ docker-compose up -d
$ cd ../../../../
$ mvn package
```

## Start system endopoints services

```sh
$ mvn spring-boot:run
```

## Stop blockchain
------------------

```sh
$ cd src/test/fixture/sdkintegration
$ docker-compose down
```
