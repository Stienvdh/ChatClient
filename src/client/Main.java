package client;

import java.net.*;

/**
 * The main class of ChatClient. It instantiates a new ChatClient with the given arguments.
 */
public class Main {
	
	/**
	 * A main method to start a new ChatClient.
	 * 
	 * @param args	The arguments to intantiate the enw ChatClient with.
	 * 
	 * @effect 	A wrong usage of the main method, i.e. not the right amount of arguments, is reported if necessary.
	 * @effect 	A new ChatClient is instantiated with the given arguments and execution of the given command is started.
	 * @effect	When the ChatClient is done doing all its necessary requests, its socket is closed. 
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{

		if (args.length != 3) {
			System.err.println("Wrong usage! Right usage: ChatClient <HTTP command> <URI> <port>");
	        System.exit(1);
		}

		String command = args[0];
		if (! args[1].startsWith("http://")) {
			args[1] = "http://" + args[1];
		}
		URL url = new URL(args[1]);
		int port = Integer.parseInt(args[2]);
	    Socket clientSocket = new Socket(url.getHost(), port);
	    
	    ChatClient chatClient = new ChatClient(command, url, port, clientSocket);
	    chatClient.executeCommand();
	    
	    System.out.println("Closing connection... \n");
	    clientSocket.close();
	}
	
}