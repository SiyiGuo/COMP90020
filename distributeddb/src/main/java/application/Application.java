package application;


import application.storage.InMemoryStorage;
import io.grpc.netty.shaded.io.netty.util.internal.logging.InternalLoggerFactory;
import io.grpc.netty.shaded.io.netty.util.internal.logging.Log4JLoggerFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import raft.concurrentutil.SleepHelper;
import raft.nodemodule.AddressBook;
import raft.nodemodule.Node;
import raft.nodemodule.NodeConfig;
import raft.nodemodule.NodeInfo;
import raft.nodemodule.RaftClientRequest;
import raft.nodemodule.RaftClientResponse;
import raft.rpcmodule.RaftRpcClient;
import raft.statemachinemodule.RaftCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {
    public static final String MODE = "mode";
    public static final String PORT = "port";
    public static final String PEER = "peer";
    public static final String CONTROLLER = "controller";
    public static final String HOST_ADDRESS = "host_address";

    public static final HashSet<Integer> PORTS = new HashSet<Integer>(Arrays.asList(8258, 8259, 8260));
    public static final HashMap<Integer, NodeInfo> ALL_NODES = new HashMap<>();

    public static void main(String[] args) {
        // address initiate

        Options options = new Options();
        Option programMode = new Option("m", MODE, true, "Peer or Controller");
        Option listenPort = new Option("p", PORT, true, "Port peer will listen to");
        Option hostAddress = new Option("h", HOST_ADDRESS, true, "host of this node");

        programMode.setRequired(true);
        hostAddress.setRequired(true);
        options.addOption(programMode);
        options.addOption(listenPort);
        options.addOption(hostAddress);

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
        for (Integer peerPort : PORTS) {
            ALL_NODES.put(peerPort, new NodeInfo(peerPort, peerPort, cmd.getOptionValue(HOST_ADDRESS)));
        }

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
        ArrayList<RaftRpcClient> clients = new ArrayList<>();
        for (NodeInfo nodeInfo: ALL_NODES.values()) {
            clients.add(new RaftRpcClient(nodeInfo.hostname, nodeInfo.listenPort));
        }
        Random rand = new Random();

        // Start operating
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
            if (values.length != 3) {
                System.err.println("Invalid format");
                String commands = "";
                for(RaftCommand s: RaftCommand.values()) {
                    commands += s.name() + ", ";
                }
                System.out.println("Command format: " + commands);
                continue;
            }

            // normal operation
            try {
                RaftRpcClient selectedClient = clients.get(rand.nextInt(clients.size()));
                RaftClientResponse response = selectedClient.handleClientRequest(
                        new RaftClientRequest(
                                RaftCommand.valueOf(values[0]),
                                values[1],
                                values[2]
                        )
                );
                System.out.println(response);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void NodeMode(int listenPort) {
        System.out.println("Node mode at port: "+listenPort);
        // TODO: check listen port is in our peer set;;
        if (!(PORTS.contains(listenPort))) {
            System.out.println("Please one of the following port: ");
            PORTS.forEach((p) -> System.out.println(p));
            System.exit(1);
            return;
        }

        NodeConfig config = new NodeConfig();
        Node node = new Node(config,
                new AddressBook(
                        ALL_NODES.get(listenPort),
                        ALL_NODES.values().toArray(new NodeInfo[PORTS.size()])),
                new InMemoryStorage()
        );
        node.run();
        while(true) {
            // so that we can monitor the package
            SleepHelper.sleep(1000000);
        }
    }
}