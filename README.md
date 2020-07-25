# A word-count program using Apache-Kafka

This is an example program showing how to make a producer for Kofka 
and consume the produced data using the Kofka streams API. 

The program requests text blocks from https://baconipsum.com/ and counts the
words. Finally it outputs the most common word for each generated text block.

The program works in 3 steps

1. Create a producer for a Kafka topic "text"

	* continually request and push random text from baconipsum
	* start this process on a separate co-routine

2. Consume the generated text via the streams API and map it to 
   JSON for a `Map<String, int>`. Output that stream to the "wordcounts"
   topic

3. Consume the wordcounts topic via the strams API. Deserialize the JSON `Map<String, Int>`
   and find the entry with the highest wordcount. Finally print the entries as they are found.

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

Then to run the wordcount program

```
java -jar ./KotlinExample/build/libs/KotlinExample-1.0-all.jar
```

