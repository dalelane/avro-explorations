package uk.co.dalelane.kafka.avro;

import java.io.File;

public class MyAvroProducers extends Demos {

    public static void main(final String[] args) throws Exception {

        sendMessages(
            "DEMO.APPENDING",
            loadConfig(args[0]),
            new File("./schemas/appending-new-field"));

        sendMessages(
            "DEMO.PREPENDING",
            loadConfig(args[0]),
            new File("./schemas/prepending-new-field"));

        sendMessages(
            "DEMO.INSERTING",
            loadConfig(args[0]),
            new File("./schemas/inserting-new-field"));
    }
}