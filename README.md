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

The output will look like

```
...
3ae8b4e5-00fb-4994-9606-1d198c7adeeb, (pork, 11.0)
b25d16c2-c864-413f-82ea-b93606e79fcc, (beef, 7.0)
435b4099-d3f8-42af-a784-07e268af76b1, (pork, 15.0)
02304ad1-2dcb-4a3b-82cc-8adc87fc57c1, (pork, 7.0)
c2492aca-b269-4713-a76c-ca1ae454be94, (pork, 9.0)
1db33f63-653a-4092-8f01-387446848de5, (in, 5.0)
341d33c1-b212-4a94-8f85-9e99cba112e4, (in, 10.0)
3a2e9fd3-cf54-4ecd-b490-5f25b0c807a2, (loin, 6.0)
c383e462-cacc-4a5d-a262-92741d0dcf4f, (in, 7.0)
a9642ad4-962c-406d-83a1-58888aa10602, (pork, 7.0)
fd773cb8-bf24-4ed9-b6ec-4b1e60c7bd9b, (ut, 7.0)
6eb29a1c-0c8f-468e-a073-b8f04d27f0ce, (pork, 12.0)
b4effc9c-4185-4280-b5b0-4f80e20dea12, (beef, 11.0)
ef57b5e9-d6e7-4fe3-adad-c0ef83331490, (pork, 9.0)
963985e3-3f99-4b2d-96cd-85e82d703328, (tenderloin, 7.0)
38ab251f-584f-4c72-8c0e-a71334424730, (pork, 8.0)
3f80a913-2687-4cdf-851b-bba4878972f6, (dolore, 5.0)4
...
```

