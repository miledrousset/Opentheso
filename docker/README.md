# Building the Docker Image

The configuration for two Docker Images are provided; one image for Opentheso2 itself, and the other for a Postgres database which holds the Opentheso2 data. 

```
docker image build -t miledrousset/opentheso2-postgres -f Dockerfile-postgres .

docker image build -t miledrousset/opentheso2 .
```


# Running the Docker Containers

There are two options for running the Docker Containers:
1. Using Docker Compose to manage the containers for you
2. Using Docker directly to start each container

Pre-requisites:
1. Docker
2. Place a copy of `opentheso2-21.02.war` in the `opentheso2/docker` directory.


## Running via Docker Compose

Simply execute:

```
cd opentheso2/docker

docker compose up
```

## Running directly with Docker

A Docker Container for the Opentheso2 Posgres database must be started before starting a container for Opentheso22 itself.

```
cd opentheso2/docker

docker run --name opentheso2-db --volume opentheso2-pgdata:/pgdata --env POSTGRES_USER=opentheso --env POSTGRES_PASSWORD=opentheso --env PGDATA=/pgdata miledrousset/opentheso2-postgres

docker run --name opentheso2 --link opentheso2-db --publish 8080:8080 -it miledrousset/opentheso2
```

## Accessing Opentheso2

Once the Docker Containers are running, you can access Opentheso2 in a web-browser by visiting: http://localhost:80/opentheso2


# Docker Volumes

The docker volume `opentheso2-pgdata` stores the database, if you want to start with a clean slate you can remove the volume and it will be re-created. To remove the volume just run:

```
docker volume rm opentheso2-pgdata
```
