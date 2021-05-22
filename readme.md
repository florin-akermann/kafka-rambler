### Kafka-Rambler

### 'rambling'

#### /ˈramblɪŋ/
adjective

1. (of writing or speech) lengthy and confused or inconsequential.
   "a rambling six-hour speech"
   Opposite: concise
   
2. ...

The Kafka-Rambler tool strives to live up to its name by continuously sending arbitrary data based on the schema registered in the confluent schema registry.
The intended use case for Kafka-Rambler is integration testing where data is missing or test environments where the behavior of a system is to be observed over a longer period.
If no schema is defined for the key, or the value of a topic then the rambler resorts to random alphanumeric strings.
Messages are continuously produced. The max intensity of the 'rambling' can be configured:

      //how many elements max produced within defined duration
      ramble: {
         duration: 10ms // 10m,  10d
         elements: 100
      }

As always, the default configuration (../application.conf) can be overridden via environment variables for the containerized version or by providing 'config.file' with the path to an external configuration file as a JVM option.
See examples below, or check [HOCON](https://github.com/lightbend/config) for more details.

Currently, only avro schemas are supported.
Xsd, Json and other schemas can be supported by providing an appropriate implementation of java/li/akermann/schema/rambler/SchemaRambler.java

#### Example

prerequisite: kafkacat

In the project root run

    docker-compose up -d

to start up zookeeper, schema-registry and kafka.

Register a key- and a value-schema for the test topic by running

    ./post-schema.sh

Build the rambler image

    ./gradlew jibDockerBuild

Run the container
   
    docker run --network=host kafka-rambler

Check what is happening on the target topic

    kafkacat -b localhost:9092 -C -t test

To see the behavior when no schema is registered, run the image and overriding the 'topic.schema-source' configuration

    docker run -e CONFIG_FORCE_topic_schema__source="some-schema-less-topic" --network=host kafka-rambler

Follow the topic

    kafkacat -b localhost:9092 -C -t some-schema-less-topic

### SSL

put your SSL configurations under

    akka.ssl-config{
    //https://doc.akka.io/docs/akka-http/10.1.2/client-side/client-https-support.html
    }

### In case you want to run the app without docker:

To run create and run jar directly

    ./gradlew shadowJar
    java -jar ./build/libs/kafka-rambler-1.0.0-all.jar

or with external config override:

    ./gradlew shadowJar
    java -jar -Dconfig.file=./path/to/config.conf ./build/libs/kafka-rambler-1.0.0-all.jar