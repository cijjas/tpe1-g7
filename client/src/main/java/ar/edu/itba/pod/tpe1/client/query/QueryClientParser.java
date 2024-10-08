package ar.edu.itba.pod.tpe1.client.query;

import org.apache.commons.cli.*;

import java.nio.file.Paths;
import java.util.Optional;

public class QueryClientParser {
    Options options = new Options();
    private final CommandLineParser parser  = new GnuParser();

    public QueryClientParser() {
        // Required
        Option serverAddressOption = new Option("DserverAddress", true, "Server address");
        serverAddressOption.setRequired(true);

        Option actionOption = new Option("Daction", true, "Action");
        actionOption.setRequired(true);

        options.addOption(actionOption);
        options.addOption(serverAddressOption);
        // Options
        options.addOption("Dsector",true, "Sector name");
        options.addOption("Dcounter",true, "Counter value");
        options.addOption("Dairline",true, "Airline name");
        options.addOption("DoutPath", true, "Output path");
    }


    public Optional<QueryClientArguments> getQueryClientArguments(String[] args) {
        try {
            CommandLine cmd = parser.parse(options, args);

            QueryClientArguments arguments = new QueryClientArguments();
            // Server address
            arguments.setServerAddress(cmd.getOptionValue("DserverAddress"));
            // Action
            arguments.setAction(QueryClientAction.valueOf(cmd.getOptionValue("Daction").toUpperCase()));
            // Sector
            arguments.setSector(cmd.getOptionValue("Dsector"));
            // Airline
            arguments.setAirline(cmd.getOptionValue("Dairline"));
            // Out path
            String outPathValue = cmd.getOptionValue("DoutPath");
            arguments.setOutPath(outPathValue != null ? Paths.get(outPathValue) : null);

            return Optional.of(arguments);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

}
