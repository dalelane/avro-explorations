package uk.co.dalelane.kafka.avro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public abstract class Demos {


    protected static void consume (String topic, Properties props, File schemasFolder) throws Exception {
        //
        // retrieve schemas to use
        Schema v1Schema = getSchema(schemasFolder, "schema-v1.avsc");
        Schema v2Schema = getSchema(schemasFolder, "schema-v2.avsc");

        //
        // prepare a Kafka consumer
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dalelane");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        final Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));

        //
        // consume the two test messages
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMinutes(10));
        assert records.count() == 2;
        Iterator<ConsumerRecord<String, byte[]>> iterator = records.iterator();
        ConsumerRecord<String, byte[]> record1 = iterator.next();
        ConsumerRecord<String, byte[]> record2 = iterator.next();
        consumer.close();

        //
        // demonstrate the different ways to deserialize these messages

        System.out.println("*********************************************************************");
        System.out.println("Using schemas from " + schemasFolder.getName());
        System.out.println("*********************************************************************");

        System.out.println("\n===================================================================");
        System.out.println("Unusual option 1:");
        System.out.println("Use the v1 schema (for both reader and writer) for both messages");
        System.out.println("-------------------------------------------------------------------");

        deserialize(v1Schema, v1Schema, record1.value());
        deserialize(v1Schema, v1Schema, record2.value());

        System.out.println("\n===================================================================");
        System.out.println("Unusual option 2:");
        System.out.println("Use the v2 schema (for both reader and writer) for both messages");
        System.out.println("-------------------------------------------------------------------");

        deserialize(v2Schema, v2Schema, record1.value());
        deserialize(v2Schema, v2Schema, record2.value());

        System.out.println("\n===================================================================");
        System.out.println("Unusual option 3:");
        System.out.println("Use the v1 schema for writer, and v2 schema for reader");
        System.out.println("-------------------------------------------------------------------");

        deserialize(v1Schema, v2Schema, record1.value());
        deserialize(v1Schema, v2Schema, record2.value());

        System.out.println("\n===================================================================");
        System.out.println("Unusual option 4:");
        System.out.println("Use the v2 schema for writer, and v1 schema for reader");
        System.out.println("-------------------------------------------------------------------");

        deserialize(v2Schema, v1Schema, record1.value());
        deserialize(v2Schema, v1Schema, record2.value());

        System.out.println("\n===================================================================");
        System.out.println("Correct option 1:");
        System.out.println("Use the correct schema for writer, and v1 schema for reader");
        System.out.println("-------------------------------------------------------------------");

        deserialize(v1Schema, v1Schema, record1.value());
        deserialize(v2Schema, v1Schema, record2.value());

        System.out.println("\n===================================================================");
        System.out.println("Correct option 2:");
        System.out.println("Use the correct schema for writer, and v2 schema for reader");
        System.out.println("-------------------------------------------------------------------");

        deserialize(v1Schema, v2Schema, record1.value());
        deserialize(v2Schema, v2Schema, record2.value());

        System.out.println("\n===================================================================");
    }


    private static void deserialize(Schema writer, Schema reader, byte[] data) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        BinaryDecoder decoder = DecoderFactory.get().directBinaryDecoder(input, null);

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(writer, reader);

        try {
            GenericRecord message = datumReader.read(null, decoder);
            System.out.println(message.toString());
        }
        catch (Exception e) {
            System.out.println("UNABLE TO DESERIALIZE (" + e.getClass().getName() + ")");
        }
    }




    protected static void sendMessages(String topic, Properties props, File schemasFolder) throws Exception {
        //
        // prepare a Kafka producer
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        Producer<String, byte[]> producer = new KafkaProducer<>(props);

        //
        // get the schemas to use
        Schema v1Schema = getSchema(schemasFolder, "schema-v1.avsc");
        Schema v2Schema = getSchema(schemasFolder, "schema-v2.avsc");

        // send message one
        GenericRecord messageOne = new GenericData.Record(v1Schema);
        messageOne.put("myMessage", "This is message one, produced using the v1 schema");
        messageOne.put("myFloat", 1.1f);
        messageOne.put("myInt", 111);
        sendMessage(producer, topic, messageOne, "v1");

        // send message two
        GenericRecord messageTwo = new GenericData.Record(v2Schema);
        messageTwo.put("myMessage", "This is message two, produced using the v2 schema");
        messageTwo.put("myFloat", 2.2f);
        messageTwo.put("myInt", 222);
        messageTwo.put("myBiscuit", "hobnob");
        sendMessage(producer, topic, messageTwo, "v2");

        //
        // finish
        producer.flush();
        producer.close();
    }



    private static void sendMessage(Producer<String, byte[]> producer, String topic, GenericRecord record, String schema) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(output, null);

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(record.getSchema());
        datumWriter.write(record, encoder);

        encoder.flush();
        output.close();

        ProducerRecord<String, byte[]> encodedMessage = new ProducerRecord<>(topic, null, output.toByteArray());
        producer.send(encodedMessage);

        System.out.println("Sending to " + topic);
        System.out.println("    " + record.toString());
    }







    protected static Schema getSchema(File folder, String avsc) throws IOException {
        return new Schema.Parser().parse(new File(folder, avsc));
    }

    protected static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}
