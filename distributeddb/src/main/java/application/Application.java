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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    public static final List<Integer> ALL_PORTS = Arrays.asList(8258, 8259, 8260);
    public static final HashSet<Integer> PORTS = new HashSet<Integer>(ALL_PORTS);
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

        // May use random to selec
        Random rand = new Random();
        // Create all RPCclient for requesting
        HashMap<Integer, RaftRpcClient> clients = new HashMap<>();
        ALL_NODES.values().forEach(nodeInfo -> clients.put(nodeInfo.nodeId,
                                                        new RaftRpcClient(nodeInfo.hostname, nodeInfo.listenPort)));

        // Start operating
        String commands = "";
        for(RaftCommand s: RaftCommand.values()) { commands += s.name() + ", "; }
        System.out.println("Please use command: "+commands);
        System.out.println("Command format: COMMAND,KEY,VALUE\n");

        // start reading user input
        String input;
        String chosedNode;
        Scanner scanner = new Scanner(System.in);
        RaftRpcClient selectedClient;
        while (true) {
            System.out.println("Please choose client from 8258, 8259, 8260. Empty for random: " );
            chosedNode = scanner.nextLine();
            try {
                selectedClient = clients.get(Integer.parseInt(chosedNode));
                System.out.println("Sending to node: " + chosedNode);
            } catch (Exception e) {
                int tmp = ALL_PORTS.get(rand.nextInt(ALL_PORTS.size()));
                selectedClient = clients.get(tmp);
                System.out.println("Sending to node: " + tmp);
            }

            input = scanner.nextLine();
            if (input.equalsIgnoreCase("stop")) { break; }

            try {
                String[] values = input.split(",", 3);
                RaftCommand command = RaftCommand.valueOf(values[0].toUpperCase());

                RaftClientResponse response;
                switch (command) {
                    case GETSTORAGE:
                    case GETALLLOGS:
                    case FINDLEADER:
                        response = selectedClient.handleClientRequest(new RaftClientRequest(command, "", ""));
                        break;
                    case GET:
                    case PUT:
                    case UPDATE:
                    default:
                        response = selectedClient.handleClientRequest(
                                new RaftClientRequest(
                                        command,
                                        values[1],
                                        values[2]
                                )
                        );
                        break;

                }
                System.out.println(response);
            } catch (Exception e) {
                System.out.println("Please use command: "+commands);
                System.out.println("Command format: COMMAND,KEY,VALUE\n");
                System.err.println(e.getMessage());
            }
        }
    }

    public static void NodeMode(int listenPort) {
        System.out.println("Node mode at port: "+listenPort);
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