package client;

import java.io.*;
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

	public void executePUT() {
		// TODO Auto-generated method stub
		
	}

	public void executeHEAD() {
		// TODO Auto-generated method stub
		
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
	    sentence += "host: " + url.getHost() + ":" + port;
	    System.out.println("TO SERVER: " + "\n");
	    System.out.println(sentence + "\n");
	    this.outToServer.writeBytes(sentence);
	    this.outToServer.writeBytes("\r\n\r\n");
	    
	    String header = this.getHeader(this.inFromServer);
	    byte[] body = this.getBody(header, inFromServer);
	    System.out.println("FROM SERVER: \n");
	    System.out.print(header);
	    System.out.print(new String(body) + "\n");
	    
	    File file = new File("response.html");
		BufferedWriter fileWrite = new BufferedWriter(new FileWriter(file));
	    fileWrite.write(new String(body)); 
	    fileWrite.close();
	    
	    this.FetchImages(new String(body));
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
		line.trim();
		int startLength = line.indexOf(":") + 2;
		String lengthString = line.substring(startLength);
		int length = Integer.parseInt(lengthString);
		byte[] body = new byte[length];
		
		int offset = 0;
		while(length != offset){
			offset += inFromServer.read(body, offset, length - offset);
		}
		
		return body;
	}
	
	public void FetchImages(String body) throws IOException {
		Document doc = Jsoup.parse(body);
	    Elements images = doc.select("img[src]");
	    System.out.println("Number of images found: " + images.size() + "\n");
	    
	    
	    
	    if (images.size() > 0) {
	    	
	    	for (int i=0; i < images.size(); i++) {
	    		System.out.println(images.get(i));
	    		System.out.print("Fetching image " + (i+1) + " of " + images.size() + "... ");
	    		String source = images.get(i).attr("src");
	    		String extension = FilenameUtils.getExtension(source);
	    		File imageFile = new File("image_" + (i+1) + "." + extension);
	    		FileOutputStream imageFileWrite = new FileOutputStream(imageFile);
	    		
	    		String sentence = "GET" + " /" + source + " " + "HTTP/1.1" + "\r\n";
	    		sentence += "host: " + url.getHost() + ":" + this.port;
	    		inFromServer = new BufferedInputStream(this.socket.getInputStream());
	    	    outToServer.writeBytes(sentence);
	    	    outToServer.writeBytes("\r\n");
	    	    outToServer.writeBytes("\r\n");
	    	    
	    	    String header = this.getHeader(inFromServer);
	    	    byte[] image = this.getBody(header, inFromServer);
	    	    imageFileWrite.write(image);
	    	    imageFileWrite.close();
	    	    
	    	    System.out.println("Done");
	    	}
	    	System.out.println("\nFetched all images \n");
	    }
	}

	public String command;
	public URL url;
	public int port;
	public InputStream inFromServer;
	public DataOutputStream outToServer;
	public Socket socket;
	
}
