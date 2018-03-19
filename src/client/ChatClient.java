package client;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * A class that implements a ChatClient. A ChatClient is able to process GET-, HEAD-, PUT- and POST-commands by the 
 * HTTP 1.1 protocol and uses TCP sockets to do so. In case of a GET-command, the ChatClient is able to retrieve the
 * images, embedded in the requested HTML-page and locally save them. 
 * 
 * @invar	A ChatClient will always be instantiated with a command, an url, a port and a socket.
 * @invar	A ChatClient will always maintain a persistent connection with its socket. Only when it has to fetch an image from 
 * 			a different host, it closes its connection after each image. 
 */
public class ChatClient {
	
	/**
	 * Create a new ChatClient with a given command, url, port and socket.
	 * 
	 * @param command			The HTTP command to execute	(GET, HEAD, PUT or POST)	
	 * @param url				The URL to connect to
	 * @param port				The port to connect to
	 * @param socket			The socket to communicate with
	 * 
	 * @post	The new ChatClient is instantiated with the given arguments. It now has an input- and outputstream,
	 * 			by which communication with the given socket is made possible.
	 */
	public ChatClient(String command, URL url, int port, Socket socket) {
		setCommand(command);
		setURL(url);
		setPort(port);
		setSocket(socket);
		try {
			setOutToServer(new DataOutputStream(socket.getOutputStream()));
			setInFromServer(new BufferedInputStream(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reroute execution to the method, that implements the requested command. 
	 * 
	 * @effect	This method calls the appropriate method, in correspondance with the command it is initiated with
	 * 			@see implementation
	 */
	public void executeCommand() throws IOException {
		if (getCommand().equals("GET")) {
			executeGET();
		}
		else if (getCommand().equals("HEAD")) {
			executeHEAD();
		}
		else if (getCommand().equals("PUT")) {
			executePUT();
		}
		else if (getCommand().equals("POST")) {
			executePOST();
		}
	}

	public void executePOST() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Execute a PUT request.
	 * 
	 * @effect	The method prompts the user for the sentence to send to the host. 
	 * @effect	The method creates a request to send to the host. This request contains the PUT command with URL and HTTP version 1.1,
	 * 			the appropriate host header, the appropriate Content-Length header and the body the user wants to send. 
	 * @effect	The method sends this header and body to the host. 
	 */
	public void executePUT() {
		String path;
	    if (getURL().getPath() == "") {
	    	path = "/";
	    }
	    else {
	    	path = url.getFile();
	    }
		String sentence = command + " " + path + " " + "HTTP/1.1" + "\r\n";
	    sentence += "Host: " + url.getHost() + ":" + port + "\r\n";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("\nTell us a joke: ");
		String stringToSend = "";
		try {
			stringToSend = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sentence += "Content-Length: " + stringToSend.length() + "\r\n";
		
		System.out.println("\nTO SERVER: " + "\n");
	    System.out.println(sentence);
	    try {
			getOutToServer().writeBytes(sentence + "\r\n");
			getOutToServer().writeBytes(stringToSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    String response = this.getHeader(getInFromServer());
	    System.out.println("FROM SERVER: \n");
	    System.out.print(response);
	}
	
	/**
	 * Execute a HEAD request. 
	 * 
	 * @effect	The method creates a request to send to the host. This request contains the HEAD command with URL and 
	 * 			HTTP version 1.1 and the appropriate host header.
	 * @effect 	The method sends this header to the host. 
	 */
	public void executeHEAD() {
		String path;
	    if (getURL().getPath() == "") {
	    	path = "/";
	    }
	    else {
	    	path = getURL().getFile();
	    }
	    
	    String sentence = command + " " + path + " " + "HTTP/1.1" + "\r\n";
	    sentence += "Host: " + getURL().getHost() + ":" + port;
	    System.out.println("TO SERVER: " + "\n");
	    System.out.println(sentence + "\n");
	    try {
	    	getOutToServer().writeBytes(sentence);
		    getOutToServer().writeBytes("\r\n\r\n");
	    }
	    catch (IOException exc) {
	    	exc.printStackTrace();
	    }
	    
	    String header = this.getHeader(getInFromServer());
	    System.out.println("FROM SERVER: \n");
	    System.out.print(header);
	}
	
	/**
	 * Execute a GET request. 
	 * 
	 * @effect	The method creates a request to send to the host. This request contains the GET command with URL and 
	 * 			HTTP version 1.1 and the appropriate host header.
	 * @effect 	The method sends this header to the host.
	 * @effect	If the host responds with a HTML file, the method starts fetching the images in the file.
	 * 			| fetchImages();
	 */
	public void executeGET() throws IOException {
		String path;
	    if (getURL().getPath() == "") {
	    	path = "/";
	    }
	    else {
	    	path = getURL().getFile();
	    }
	    
	    String sentence = command + " " + path + " " + "HTTP/1.1" + "\r\n";
	    sentence += "Host: " + getURL().getHost() + ":" + port;
	    System.out.println("TO SERVER: " + "\n");
	    System.out.println(sentence + "\n");
	    getOutToServer().writeBytes(sentence);
	    getOutToServer().writeBytes("\r\n\r\n");
	    
	    String header = this.getHeader(getInFromServer());
	    System.out.println("FROM SERVER: \n");
	    System.out.print(header);
	    byte[] body = this.getBody(header, getInFromServer());
	    System.out.print(new String(body) + "\n");
	    
	    String respons = this.FetchImages(new String(body));
	    
	    File file = new File("response.html");
		BufferedWriter fileWrite = new BufferedWriter(new FileWriter(file));
	    fileWrite.write(respons); 
	    fileWrite.close();
	}
	
	/**
	 * Returns the header, sent to the ChatClient by the server. 
	 * 
	 * @param inFromServer	The inputstream that contains the header to read. 
	 * 
	 * @effect	The method reads the inputstream byte-per-byte, until it detects the end of the header sent by the server
	 * 			by the presence of the "\r\n\r\n"-String.
	 * 
	 * @return	The method returns a string, that corresponds to the header on the given inputstream.
	 */
	public String getHeader(InputStream inFromServer) {
		byte[] header = new byte[1000000];
	    int offset = 0;
	    while (! (new String(header)).contains("\r\n\r\n")) {
	    	try {
				offset += inFromServer.read(header, offset, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return new String(header);
	}
	
	/**
	 * Returns the body, sent by the server. 
	 * 
	 * @param header		The header, sent by the server, corresponding to the body to return. 
	 * @param inFromServer	The inputstream, the body has to be read off.
	 * 
	 * @effect	The method deducts from the 'Content-Length' header how many bytes it has to erad from the inputstream and then
	 * 			reads that amount of bytes into the byte array it then returns. 
	 * 
	 * @return	The method returns a byte array, containing the body the server sent in response to the request of this ChatClient.
	 */
	public byte[] getBody(String header, InputStream inFromServer) throws IOException {
		int startLengthHeader = header.indexOf("Content-Length");
		int endLengthHeader = header.indexOf("\r", startLengthHeader);
		String line = header.substring(startLengthHeader, endLengthHeader);
		line = line.trim();
		int startLength = line.indexOf(":") + 2;
		String lengthString = line.substring(startLength);
		int length = Integer.parseInt(lengthString);
		System.out.println("length body: " + length);
		byte[] body = new byte[length];
		
		int offset = 0;
		while(length != offset){
			offset += inFromServer.read(body, offset, length - offset);
		}
		return body;
	}
	
	/**
	 * A method to fetch images, embedded in the HTML file the server sent in respnse to a GET request of the ChatClient and
	 * save them locally.
	 * 
	 * @param body	The HTML file the server sent. 
	 * 
	 * @effect	The method first creates a directory in which it will store the images. 
	 * @effect	For each image, the method executes a GET request for the page, where the image is stored.
	 * @effect	If the image is not stored on the current host the ChatClient is connected to, the method opens a 
	 * 			new connection to the host, where the image is located and closes it after fetching the image. 
	 *			Otherwise, the ChatClient sends its GET request over the persistent connection it has with its host. 
	 * @effect	Each image is locally stored in the directory created earlier. 
	 * 
	 * @return 	The method return a new HTML file, in which references to images on the server are now references to the images
	 * 			fetched and locally stored. 
	 */
	public String FetchImages(String body) throws IOException {
		Document doc = Jsoup.parse(body);
	    Elements images = doc.select("img[src]");
	    System.out.println("Number of images found: " + images.size() + "\n");
	    
	    String dirName = "/Users/Stien/Documents/School/3de bach/Computer Networks/Images";
	    File dir = new File(dirName);
	    dir.mkdir();
	    
	    if (images.size() > 0) {
	    	
	    	for (int i=0; i < images.size(); i++) {
	    		String source = images.get(i).attr("src");
	    		System.out.print("Fetching image " + (i+1) + " of " + images.size() + "... ");
	    		String extension = FilenameUtils.getExtension(source);
	    		body = body.replace(source, dirName + "\\image_" + (i+1) + "." + extension);
	    		File imageFile = new File(dirName, "image_" + (i+1) + "." + extension);
	    		FileOutputStream imageFileWrite = new FileOutputStream(imageFile);
	    		
	    		String sentence = "";
	    		String host = getURL().getHost();
	    		Socket imageSocket = getSocket();
	    		boolean newSocket = true;
	    		
	    		try {
	    			URL sourceURL = new URL(source);
	    			System.out.println(sourceURL);
	    			host = sourceURL.getHost();
	    			imageSocket = new Socket(host, 80);
	    			sentence = "GET " + source + " " + "HTTP/1.1" + "\r\n";
	    		}
	    		catch (MalformedURLException exc){
	    			newSocket = false;
	    			sentence = "GET " + "/" + source + " " + "HTTP/1.1" + "\r\n";
	    		}

	    		sentence += "Host: " + host + ":" + imageSocket.getPort();
	    		setInFromServer(new BufferedInputStream(imageSocket.getInputStream()));
	    		setOutToServer(new DataOutputStream(imageSocket.getOutputStream()));
	    	    getOutToServer().writeBytes(sentence);
	    	    getOutToServer().writeBytes("\r\n");
	    	    getOutToServer().writeBytes("\r\n");
	    	    
	    	    String header = this.getHeader(getInFromServer());
	    	    byte[] image = this.getBody(header, getInFromServer());
	    	    imageFileWrite.write(image);
	    	    imageFileWrite.close();
	    	    
	    	    if (newSocket) {
	    	    	imageSocket.close();
	    	    }
	    	    
	    	    System.out.println("Done");
	    	}
	    	System.out.println("\nFetched all images \n");
	    }
	    return body;
	}
	
	/**
	 * Set the inputstream of this ChatClient to the given inputstream.
	 * 
	 * @param inFromServer	The new inputstream
	 * 
	 * @post	| new.getInFromServer() == inFromServer
	 */
	private void setInFromServer(BufferedInputStream inFromServer) {
		this.inFromServer = inFromServer;
	}

	/**
	 * Set the outputstream of this ChatClient to the given outputstream.
	 * 
	 * @param outToServer	The new outputstream
	 * 
	 * @post	| new.getOutToServer() == outToServer
	 */
	private void setOutToServer(DataOutputStream outToServer) {
		this.outToServer = outToServer;
	}

	/**
	 * Set the socket of this ChatClient to the given socket.
	 * 
	 * @param socket	The new socket
	 * 
	 * @post	| new.getSocket() == socket
	 */
	private void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Set the port of this ChatClient to the given port.
	 * 
	 * @param port	The new port
	 * 
	 * @post	| new.getPort() == port
	 */
	private void setPort(int port) {
		this.port = port;
	}

	/**
	 * Set the URL of this ChatClient to the given URL.
	 * 
	 * @param url	The new URL
	 * 
	 * @post	| new.getURL() == url
	 */
	private void setURL(URL url) {
		this.url = url;
	}

	/**
	 * Set the command of this ChatClient to the given command.
	 * 
	 * @param command	The new command
	 * 
	 * @post	| new.getCommand() == command
	 */
	private void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * Returns the URL of this ChatClient.
	 */
	private URL getURL() {
		return this.url;
	}
	
	/**
	 * Returns the inputstream of this ChatClient.
	 */
	private InputStream getInFromServer() {
		return this.inFromServer;
	}
	
	/**
	 * Returns the outputstream of this ChatClient.
	 */
	private DataOutputStream getOutToServer() {
		return this.outToServer;
	}
	
	/**
	 * Returns the socket of this ChatClient.
	 */
	private Socket getSocket() {
		return this.socket;
	}
	
	/**
	 * Returns the command of this ChatClient.
	 */
	private String getCommand() {
		return this.command;
	}

	public String command;
	public URL url;
	public int port;
	public InputStream inFromServer;
	public DataOutputStream outToServer;
	public Socket socket;
	
}
