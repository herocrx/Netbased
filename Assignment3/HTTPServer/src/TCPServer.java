import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TCPServer {

	private ServerSocket serverSocket;
	private Socket clientSocket=null;

	//Construnctor
	public TCPServer(){
		try {
		 serverSocket=new ServerSocket(9001);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Methods finds the name of the requested file
	private String GetFileName(String request){
		//GET /RequestedFileName HTTP/1.1
		Pattern pattern=Pattern.compile("/(.*?)HTTP/1.1");
		Matcher matcher = pattern.matcher(request);
		if (matcher.find())
		{
		return matcher.group(1);
	}
		
		return null;
	}
	//Method retriev the file format
	private String GetFileType(String fileName){
		System.out.println(fileName);
		String [] FileName=fileName.split("\\.");
		
		for(int i=0;  i<FileName.length; i++){
			System.out.println(FileName[i]);
		}
	
		if(FileName.length>=1){
		return FileName[1];
		}
		else{
			return null;
		}
	}
	
	private void sendNotImplementedError() throws IOException {
		String header = "HTTP/1.1 501 Not Implemented\r\n"
						+ "Allow: GET\r\n"
						+ "Connection: close\r\n"
						+ "\r\n"
						+"Not implemented exception";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		writer.println(header);
		writer.close();
	}

	
	
	//Sends the HTTP response
	public boolean HTTPresponse(){
		
		//Client Accept
		try {
			
			clientSocket=serverSocket.accept();
			System.out.println("Acception");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		try {
			BufferedReader in_reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String Request;
			Request=in_reader.readLine();
			System.out.println(Request);
			if(!Request.split(" ")[0].equals("GET")){
				sendNotImplementedError();
				return false;
			}
			String fileName=GetFileName(Request).trim();
			System.out.println(fileName);	
			FileInputStream fis = null;
			DataOutputStream  out= new DataOutputStream(clientSocket.getOutputStream());
			File file=new File(fileName);
			//Try to find file 
			try {
				
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("No such File");
				String ResponseString="HTTP/1.1 404 Not Found"+"Content-Type: text/html \r\n"+
						"Connection: Closed\r\n"+"\r\n"+"Error 404 File not found";
				out.writeBytes(ResponseString);
				out.flush();
				clientSocket.close();
				return false;
				
			}
			String fileFormat=GetFileType(fileName);
			System.out.println(fileFormat);
			//Check if the file is of allowed format
			if(fileFormat.contains("html") || fileFormat.contains("jpg") || fileFormat.contains("jpeg")){
			String ContentType=fileFormat.contains("html") ? " text/html":"image/jpeg";
	
			
				
			String ResponseString = "HTTP/1.1 200 OK \r\n"+
			"Content-Type:"+ContentType+"\r\n"+
			"Connection: Closed\r\n"+"\r\n";
				out.writeBytes(ResponseString);
				System.out.println(ResponseString);
				
				BufferedInputStream in = new BufferedInputStream(fis);
			    byte[] buffer = new byte[(int)file.length()];
		  
		        try {
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        int count = 0;
		        
					while ((count = in.read(buffer)) > 0){
					    out.write(buffer,0,count);	}	
	
			out.close();
				
				
				out.flush();
				fis.close();
				clientSocket.close();
			}
			else{
				String ResponseString="HTTP/1.1 400 Not Found"+"Content-Type: text/html \r\n"+
						"Connection: Closed\r\n"+"\r\n"+"Error 400 Bad Request";
				out.writeBytes(ResponseString);
				out.flush();
				clientSocket.close();
				fis.close();
				return false;
				
			}
		}
	
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
			
	
	
		
		
		
		return true;
	}



}
