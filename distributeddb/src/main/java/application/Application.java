package application;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Application {
    public static void main(String[] args) {
        Options options = new Options();
        Option programMode = new Option("p", "mode", true, "Peer or Controller");
        programMode.setRequired(true);
        options.addOption(programMode);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("-"+e.getMessage());
            formatter.printHelp("my-program", options);

            System.exit(1);
            return;
        }
        String mode = cmd.getOptionValue("mode");
        System.out.println(mode);
    }
}