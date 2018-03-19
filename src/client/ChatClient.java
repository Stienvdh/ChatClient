package client;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ChatClient {
	
	public ChatClient(String command, URL url, int port, Socket socket) throws IOException {
		this.command = command;
		this.url = url;
		this.port = port;
		this.outToServer = new DataOutputStream(socket.getOutputStream());
	    this.inFromServer = new BufferedInputStream(socket.getInputStream());
		this.socket = socket;
	}
	
	public void executeCommand() throws IOException {
		if (command.equals("GET")) {
			this.executeGET();
		}
		else if (command.equals("HEAD")) {
			this.executeHEAD();
		}
		else if (command.equals("PUT")) {
			this.executePUT();
		}
		else if (command.equals("POST")) {
			this.executePOST();
		}
	}
	
	public void executePOST() {
		// TODO Auto-generated method stub
		
	}

	public void executePUT() throws IOException {
		String path;
	    if (this.url.getPath() == "") {
	    	path = "/";
	    }
	    else {
	    	path = url.getFile();
	    }
		String sentence = command + " " + path + " " + "HTTP/1.1" + "\r\n";
	    sentence += "Host: " + url.getHost() + ":" + port + "\r\n";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("\nTell us a joke: ");
		String stringToSend = br.readLine();
		sentence += "Content-Length: " + stringToSend.length() + "\r\n";
		
		System.out.println("\nTO SERVER: " + "\n");
	    System.out.println(sentence);
	    this.outToServer.writeBytes(sentence + "\r\n");
	    this.outToServer.writeBytes(stringToSend);
	    
	    String response = this.getHeader(this.inFromServer);
	    System.out.println("FROM SERVER: \n");
	    System.out.print(response);
	}

	public void executeHEAD() throws IOException {
		String path;
	    if (this.url.getPath() == "") {
	    	path = "/";
	    }
	    else {
	    	path = url.getFile();
	    }
	    
	    String sentence = command + " " + path + " " + "HTTP/1.1" + "\r\n";
	    sentence += "Host: " + url.getHost() + ":" + port;
	    System.out.println("TO SERVER: " + "\n");
	    System.out.println(sentence + "\n");
	    this.outToServer.writeBytes(sentence);
	    this.outToServer.writeBytes("\r\n\r\n");
	    
	    String header = this.getHeader(this.inFromServer);
	    System.out.println("FROM SERVER: \n");
	    System.out.print(header);
	}

	public void executeGET() throws IOException {
		String path;
	    if (this.url.getPath() == "") {
	    	path = "/";
	    }
	    else {
	    	path = url.getFile();
	    }
	    
	    String sentence = command + " " + path + " " + "HTTP/1.1" + "\r\n";
	    sentence += "Host: " + url.getHost() + ":" + port;
	    System.out.println("TO SERVER: " + "\n");
	    System.out.println(sentence + "\n");
	    this.outToServer.writeBytes(sentence);
	    this.outToServer.writeBytes("\r\n\r\n");
	    
	    String header = this.getHeader(this.inFromServer);
	    System.out.println("FROM SERVER: \n");
	    System.out.print(header);
	    byte[] body = this.getBody(header, inFromServer);
	    System.out.print(new String(body) + "\n");
	    
	    String respons = this.FetchImages(new String(body));
	    
	    File file = new File("response.html");
		BufferedWriter fileWrite = new BufferedWriter(new FileWriter(file));
	    fileWrite.write(respons); 
	    fileWrite.close();
	}
	
	public String getHeader(InputStream inFromServer) throws IOException {
		byte[] header = new byte[1000000];
	    int offset = 0;
	    while (! (new String(header)).contains("\r\n\r\n")) {
	    	offset += inFromServer.read(header, offset, 1);
	    }
	    return new String(header);
	}
	
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
	    		String host = url.getHost();
	    		Socket imageSocket = this.socket;
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
	    		inFromServer = new BufferedInputStream(imageSocket.getInputStream());
	    		outToServer = new DataOutputStream(imageSocket.getOutputStream());
	    	    outToServer.writeBytes(sentence);
	    	    outToServer.writeBytes("\r\n");
	    	    outToServer.writeBytes("\r\n");
	    	    
	    	    String header = this.getHeader(inFromServer);
	    	    byte[] image = this.getBody(header, inFromServer);
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

	public String command;
	public URL url;
	public int port;
	public InputStream inFromServer;
	public DataOutputStream outToServer;
	public Socket socket;
	
}
