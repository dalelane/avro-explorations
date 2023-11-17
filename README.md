# avro-explorations

This is a Java application written to illustrate the blog posts:

- [You need two schemas to deserialize an Avro message… but which two?](https://dalelane.co.uk/blog/?p=5031) (dalelane.co.uk)
- [You need two schemas to deserialize an Avro message… but which two?](https://community.ibm.com/community/user/integration/blogs/dale-lane1/2023/11/16/avro-deserializing) (community.ibm.com)

It produces messages to Kafka topics, serialized as binary-encoded Avro using a variety of Avro schemas. 

It then consumes them, using different schemas to illustrate what happens if you don't use schemas correctly.

### To build:
```sh
./0-compile.sh
```

Create a file called `app.properties` and add the config for your Kafka cluster to it. 

### To produce test messages:
```sh
./1-produce.sh
```
[example output](./output-produce.txt)

### To consume test messages and display the results:
```sh
./2-consume.sh
```
[example output](./output-consume.txt)


