package application;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import raft.statemachinemodule.RaftCommand;

import java.util.Scanner;

public class Application {
    public static final String MODE = "mode";
    public static final String PORT = "port";
    public static final String PEER = "peer";
    public static final String CONTROLLER = "controller";

    public static void main(String[] args) {
        Options options = new Options();
        Option programMode = new Option("m", MODE, true, "Peer or Controller");
        Option listenPort = new Option("p", PORT, true, "Port peer will listen to");

        programMode.setRequired(true);
        options.addOption(programMode);
        options.addOption(listenPort);

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

        String mode = cmd.getOptionValue(MODE);
        if (mode.equalsIgnoreCase(PEER)) {
            String port = cmd.getOptionValue(PORT);
            NodeMode(Integer.parseInt(port));
        }

        if (mode.equalsIgnoreCase(CONTROLLER)) {
            ControllerMode();
        }
    }

    public static void ControllerMode() {
        System.out.println("controller");
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.println(String.format("Please use command: %s, %s, %s, %s",
                RaftCommand.GET.name(), RaftCommand.PUT.name(), RaftCommand.DELETE.name(), RaftCommand.UPDATE.name()));
        System.out.println("Command format: COMMAND,KEY,VALUE\n");
        while (true) {
            input = scanner.nextLine();
            System.out.println(input);
            if (input.equalsIgnoreCase("stop")) {
                break;
            }
            String[] values = input.split(",", 3);
        }
    }

    public static void NodeMode(int listenPort) {
        System.out.println("Node mode at port: "+listenPort);
        // TODO: check listen port is in our peer set;;
    }
}