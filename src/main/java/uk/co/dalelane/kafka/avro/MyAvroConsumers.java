package uk.co.dalelane.kafka.avro;

import java.io.File;

public class MyAvroConsumers extends Demos {

    public static void main(final String[] args) throws Exception {

        consume(
            "DEMO.APPENDING",
            loadConfig(args[0]),
            new File("./schemas/appending-new-field"));

        consume(
            "DEMO.PREPENDING",
            loadConfig(args[0]),
            new File("./schemas/prepending-new-field"));

        consume(
            "DEMO.INSERTING",
            loadConfig(args[0]),
            new File("./schemas/inserting-new-field"));
    }
}