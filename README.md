# A word-count program using Apache-Kafka in Docker

We use the *spotify/kafka* docker image to run Kafka and Zookeeper in 
the same container

```bash
docker pull spotify/kafka
```

To run the container on localhost use

```bash
docker run -d --name kafka --network host spotify/kafka
```

* `-d` runs the container in the background
* `--name` gives a name to the container
* `--network host` makes the container use ports from the host network

To check that the container is running use `docker ps`, you should get something like this:

```
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
f26f1456a02b        spotify/kafka       "supervisord -n"    3 minutes ago       Up 3 minutes               
 kafka
```

This project was set up in Intellij Idea as a *Kotlin* Gradle project, see the *build.gradle* for the 
required dependencies 
