import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Consumed
import java.net.URL
import java.util.*

const val BOOTSTRAP_CONFIG_ENV = "BOOTSTRAP_CONFIG"
const val DEFAULT_BOOTSTRAP_CONFIG =  "127.0.0.1:9092"
const val APPLICATION_ID_CONFIG_ENV = "APPLICATION_ID_CONFIG"
const val DEFAULT_APPLICATION_ID = "kotlin-example"
const val TEXT_TOPIC = "text"
const val WORDCOUNT_TOPIC = "wordcount"

// setup the initial properties for Kafka
val props = Properties().also {
    it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = System.getenv(BOOTSTRAP_CONFIG_ENV) ?: DEFAULT_BOOTSTRAP_CONFIG.also {
        println("running with bootstrap server $DEFAULT_BOOTSTRAP_CONFIG")
        println("to change bootstrap server set the $BOOTSTRAP_CONFIG_ENV environment variable")
    }
    it[StreamsConfig.APPLICATION_ID_CONFIG] = System.getenv(APPLICATION_ID_CONFIG_ENV) ?: DEFAULT_APPLICATION_ID.also {
        println("using application id $DEFAULT_APPLICATION_ID, to change this set the $APPLICATION_ID_CONFIG_ENV environment variable")
    }
    // use strings by default for keys and values
    it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java.name
    it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String()::class.java.name
}


val sb = StreamsBuilder()
val gson = GsonBuilder().setPrettyPrinting().create()

// read in text from TEXT_TOPIC, count the words, and write results to WORDCOUNT_TOPIC
val wcStream = sb.stream(TEXT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
    .map { key, value ->
        value.split(" ")
            .filterNot { it.isBlank() || it.isEmpty() }
            .groupBy { it }
            .map { (word, words) -> word to words.size }
            .let { KeyValue(key, gson.toJson(it.toMap())) }
    }.to(WORDCOUNT_TOPIC)

// read in the word-counts and print out the most frequent word
val mostOccurancesStream = sb.stream<String, String>(WORDCOUNT_TOPIC)
    .foreach { key, value ->
        val mostFrequent = gson.fromJson(value, mutableMapOf<String, Int>()::class.java)
            .maxBy { it.value }!!.toPair()
        println("$key, $mostFrequent")
    }

// get some random text from baconipsum.com
fun getBaconIpsum() =
    URL("https://baconipsum.com/api/?type=meat-and-filler&paras=5&format=text").openStream()
        .bufferedReader()
        .readText()
        .let { ProducerRecord(TEXT_TOPIC, UUID.randomUUID().toString(), it) }

// send some random baconipsum text every 100ms
suspend fun produceBaconIpsum(textProducer: KafkaProducer<String, String>, delay: Long) {
    generateSequence(::getBaconIpsum).forEach {
        textProducer.send(it)
        delay(delay)
    }
}

val streams = KafkaStreams(sb.build(), props)

fun main() {

    val textProducer = KafkaProducer<String, String>(Properties().apply {
        putAll(props)
        put("client.id", "bacon-ipsum-producer")
        put("key.serializer", StringSerializer::class.java.name)
        put("value.serializer", StringSerializer::class.java.name)
    })

    GlobalScope.launch { produceBaconIpsum(textProducer) }

    streams.cleanUp()
    streams.start()
    streams.localThreadsMetadata().forEach(::println)

    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}

