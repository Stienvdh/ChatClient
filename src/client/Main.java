package client;

import java.net.*;

public class Main {
	
	public static void main(String[] args) throws Exception {

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
	    
	    System.out.println("Closing connection...");
	    clientSocket.close();
	}
	
}