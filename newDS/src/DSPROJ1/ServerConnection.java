package DSPROJ1;

//Aditya Ravikumar
//1001672163

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/*Class ServerConnection
 * 
 * For Server Connection to initiate the server side programming.*/
public class ServerConnection {
	private static int port = 8005;							//port for server to start
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static DataOutputStream dataOutputStream;
	private JFrame jFrame;
	ServerClientHandler serverClientHandler=null;
	List<DataOutputStream> listDataOutputStream= new ArrayList<DataOutputStream>();
	/*Text area to show all logs*/
	private static JTextArea jTextArea;
	
	/*Text area to show all available users online*/
	private static JTextArea jTextAreaUserNames;
	/*HashSet to store all available users online*/
	Set<String> userNames= new HashSet<>();
	Map<String, DataOutputStream> userDataOutputStreamMap= new HashMap<>();

	/*Default Constructor 
	 * 
	 * It is to initiate and create Swing based JFrame window*/
	public ServerConnection() {
		constructWindow();
	}

	/*Method constructWindow 
	 * 
	 * It will create a Swing based Jframe and put all the components and add listerners to components.*/
	private void constructWindow() {
		jFrame = new JFrame("SERVER");					//create a Swing JFrame	
		jFrame.setBounds(0, 0, 550, 8500);
		jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jFrame.addWindowListener(new java.awt.event.WindowAdapter() {		//https://stackoverflow.com/questions/13207519/adding-a-new-windowlistener-to-a-jframe
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {
		            System.exit(0);
		    }
		});
		jFrame.getContentPane().setLayout(null);			//For manual positioning
		
		JScrollPane jScrollPane = new JScrollPane();		//Create ScrollPane for TextArea 
		jScrollPane.setBounds(0, 20, 550, 625);
		jFrame.getContentPane().add(jScrollPane);
		
		jTextArea = new JTextArea();					//Create TextArea to print infp
		jScrollPane.setViewportView(jTextArea);
		jTextArea.setEditable(false);
		
		Thread t = new Thread () {
			@Override
			public void run() {
				startServerConnection();			//Start the operation
			};
		};
		t.start();
		
		JLabel userLabel = new JLabel("Available Users");	// Create label
		userLabel.setBounds(10, 680, 195, 14);
		jFrame.getContentPane().add(userLabel);	
		
		JScrollPane jScrollPaneUserNames = new JScrollPane();			//Create ScrollPane for TextArea 
		jScrollPaneUserNames.setBounds(10,700, 100, 150);
		jFrame.getContentPane().add(jScrollPaneUserNames);
		
		jTextAreaUserNames = new JTextArea();						//Create TextArea to print Real Time User Info
		jScrollPaneUserNames.setViewportView(jTextAreaUserNames);
		jTextAreaUserNames.setEditable(false);
		
		JButton jButtonExit = new JButton("Exit");					//Exit Functionality to exit the Frame
		jButtonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		jButtonExit.setBounds(200, 830, 89, 23);
		jFrame.getContentPane().add(jButtonExit);
	}
	
	/*Method startServerConnection 
	 * 
	 * It creates Socket server on localhost ipaddress and the port.*/
	protected void startServerConnection() {
		try {
			serverSocket = new ServerSocket(port);				//Create ServerSocket Connection on the localhost
			jTextArea.append("Server is Up.\n");
			while(true) {
				clientSocket = serverSocket.accept();			//create clientSocket to start accepting the request
				dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
				
				listDataOutputStream.add(dataOutputStream);
				if(serverClientHandler==null)
				{
				serverClientHandler = new ServerClientHandler(clientSocket, dataOutputStream, listDataOutputStream);
				serverClientHandler.start();
				}
				else {
					serverClientHandler = new ServerClientHandler(clientSocket, dataOutputStream, listDataOutputStream);
					serverClientHandler.start();
				}
				
			}
		}
		catch (IOException e) {
			jTextArea.append("IOException. Check connection. \n");
		}
	}
	
	/*Inner class ServerClientHandler
	 * 
	 * It is for the Server and Client message handling. It handles DataInputStream and DataOutputStream objects*/
	public class ServerClientHandler extends Thread {					//inner class to handle I/O data streams
		private Socket clientSocket;
		private String clientName;
		private DataInputStream dataInputStream;
		private DataOutputStream dataOutputStream;
		private List<DataOutputStream> listDataOutputStream= new ArrayList<DataOutputStream>();
		
		public ServerClientHandler(Socket client, DataOutputStream outputStream, List<DataOutputStream> listDataOutputStream2) {
			this.clientSocket = client;
			this.dataOutputStream= outputStream;
			listDataOutputStream=listDataOutputStream2;
			try {
				dataInputStream = new DataInputStream(clientSocket.getInputStream());		//Accept the data from client Socket
			} catch (Exception e) {
				e.getMessage();
			}
		}
		

		/*Overrided run method 
		 * 
		 * It is for Server and Client message handling*/
		@Override
		public void run() {											//Run method to handle all the Server Client Communications
			String line = "",msgin;
			String arr[];
			try {
				while(true) {
					line = dataInputStream.readUTF();					//Read the data input Stream
					arr = line.split("\n");
					msgin = arr[0].split("/")[1];	
					if(msgin.contains("{")) {
						clientName = arr[0].substring(arr[0].indexOf("{")+1,arr[0].indexOf("}")); // Get the Client Name 
						jTextArea.append(line);
						dataToClient("CONNECTED:"+clientName, dataOutputStream);		//Send Connected message to Client
						userNames.add(clientName);
						userDataOutputStreamMap.put(clientName, dataOutputStream);
						jTextAreaUserNames.setText("");
						for (String string : userNames) {
							jTextAreaUserNames.append(string+"\n");							//adding Users in Text Area
						}
						
					}
					else if(msgin.contains("[1-to-1")) {
						Set<String> tempList = new HashSet<>();
						tempList.addAll(userNames);
						tempList.remove(clientName);
						System.out.println(userNames);
						String s= String.join(", ", tempList);
						dataToClient("CLIENTS:"+s, dataOutputStream);			//Send Connected message to Client
					}
					else if(msgin.contains("1-to-CLIENT")){
						jTextArea.append(line);
						jTextArea.append(clientName+": "+msgin+"\n");
						dataToClient("Message is: "+msgin+" "+clientName, userDataOutputStreamMap.get(msgin.split("#")[0].substring(1)));
					}
					else {
						jTextArea.append(line);
						jTextArea.append(clientName+": "+msgin+"\n");
						for(DataOutputStream d: listDataOutputStream) 
						{
							if((!d.equals(dataOutputStream))){
								dataToClient("Message is: "+msgin+" from "+clientName, d);
							}
						}
					}
				}					
			} 
			catch (IOException e) {
					jTextArea.append("\n"+clientName+" has left.\n\n");
					userNames.remove(clientName);						//Removing Users from Set
					jTextAreaUserNames.setText("");
					for (String string : userNames) {
						jTextAreaUserNames.append(string+"\n");			//Removing Users from Text Area
					}
				}
			}
		}
	
	/*Method dataToClient 
	 * 
	 * this method will send the message to the server on DataOutputStream object*/
	public void dataToClient(String msg, DataOutputStream dataOutputStream) {		//Send data to Client	
		try {
			dataOutputStream.writeUTF(msg);
		} catch (Exception e) {
			
		}
	}
	
	/*Main method 
	 * 
	 * It is called first when the class is run. It is a starting point of an application*/
	public static void main(String[] args) {					//Main will be invoked on starting application
		ServerConnection serverConnection = new ServerConnection();				// Start the Client Application
		serverConnection.jFrame.setVisible(true);		
	}	
}
	