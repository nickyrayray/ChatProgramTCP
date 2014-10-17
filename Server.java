/*This is where the magic happens. This class is responsible for all of
 * the methods the server is supposed to support. It is a multithreaded program
 * that supports instances of the client program through threads defined in the
 * ClientInstance class. This is also where all the methods are defined. The 
 * ClientInstance class is a private class defined here. */

import java.util.Scanner;
import java.io.*;
import java.net.*;

public class Server{

    private static final long BLOCK_TIME = 60; //in seconds
    private static final long LAST_HOUR = 3600; //in seconds
    private static final long TIME_OUT = 1800; //in seconds
    private static final String CLOSE_MSG = "CLOSESOCK"; //Tells client to close the socket on their side

    private static UserList Users;//List of Users that can be logged into
    private static UserList blocks;//List of username/IP combinations blocked for failed login

    //Initial method run from main to load all the users into memory
    public static void loadUsers(){
	Users = new UserList();
	blocks = new UserList();
	Scanner s;
	StringBuffer inFile;
	String username = null;
	String password = null;
	try{
	    s = new Scanner(new File("user_pass.txt"));
	    inFile = new StringBuffer();
	    while(s.hasNext()){
		inFile.append(s.next());
		username = inFile.toString();
		inFile.delete(0,inFile.length());
		inFile.append(s.next());
		password = inFile.toString();
		inFile.delete(0,inFile.length());
		Users.add(username, password);
	    }
	} catch (FileNotFoundException e){
	    System.out.println("File not found! Exiting...");
	    System.exit(1);
	} catch (NullPointerException e){
	    System.out.print(username + " " + password);
	}
    }


    /* Where the magic happens within where the magic happens. 
     * When new threads are created after a connection is accepted,
     * the a new instance of this class is created and run. It supports
     * all the methods described in the assignment description. */
    private static class ClientInstance implements Runnable{
	
	private Socket clntSock;//So we can access the socket just opened
	private UserList Users;//List of users available
	private User client;//User client logged into

	public ClientInstance(Socket clntSock, UserList Users){
	    this.clntSock = clntSock;
	    this.Users = Users;
	}

	/* Used to convert IP to 0.0.0.0 formet from SocketAddress format */
	public String IPString(SocketAddress change){
	    StringBuffer x = new StringBuffer(change.toString());
	    String IP = x.substring(1, x.indexOf(":"));
	    return IP;
	}

	/* Run right after new thread is created. This performs the duties associated
	 * with login. */
	public User login(BufferedReader in, PrintWriter out) throws IOException{
	    String username = null;
	    String password;
	    User found = null;
	    int count;
	    User check = null;
	    for(count = 1; count <= 3; count++){
		out.println("Enter username: ");
		username = in.readLine();

		/*Checks to see if the username/IP combo
		 * is blocked by the server. If so, login resets
		 * and continues */
		if((check = blocks.find(username)) != null){
		    if(IPString(clntSock.getRemoteSocketAddress()).equals(check.password) && ((System.currentTimeMillis() -
				check.timeout) < (BLOCK_TIME * 1000))){
			out.println("Specified user is blocked for this IP Address");
			count = 0;
			continue;
		    }
		}

		out.println("Enter password: ");
		password = in.readLine();

		//If recently entered username does not equal previous one, reset count
		if(found != null && !found.username.equals(username))
		    count = 1;

		if((found = Users.find(username)) != null){

		    //Login succeeds and returns the user successfully accessed
		    if(found.password.equals(password) && !found.status.equals("Active")){
    			out.println("Successfully logged in! Welcome to Nick's chat program!");
    			return found;
		    
		    //Username and password don't match	
    		    } else if (!found.password.equals(password)){
    			out.println("Incorrect password! Attempts remaining: " + (3 - count));
			continue;

		    //User is already online
		    } else {
			out.println("Specified User already online! Please select another username!");
			count = 0;
			continue;
		    }

		//Couldn't find the user, no count penalty
		} else {
		    out.println("Username not found! Please try again!" );
		    count = 0;
		}
	    }

	    /*Login hasn't been successful for three consecutive tries.
	     * User is created and added to the blocklist to store the username/IP
	     * combination and will be used to prevent this combination from entering
	     * the program for BLOCK_TIME seconds*/
	    out.println("Login failure! Your IP address will be blocked for this user!");
	    found = new User(username, IPString(clntSock.getRemoteSocketAddress()));
	    found.timeout = System.currentTimeMillis();
	    blocks.addBlock(found);
	    return null;
	}

	/*Supports the whoelse method defined in the assignment parameters.
	 * Prints the users that are logged in to the requesting client.*/
	public void whoelse(PrintWriter out, User client){
	    User current = Users.head;
	    int count = 0;
	    while(current != null){ //Steps through the list to see who is online
		
		//Prints those who are not the client and are online
		if(current.status.equals("Active") && current != client){
		    count++;
		    out.println(current.username);
		}
		//Iterates through the list of Users
		current = current.next;
	    }
	    //No one is online. So outputs just that.
	    if(count == 0)
		out.println("No one else online!");
	}

	/*Outputs users currently online and who were online in
	 * LAST_HOUR to the requesting client. Does not include the requesting
	 * client.*/
	public void wholasthr(PrintWriter out, User client){
	    User current = Users.head;
	    int count = 0;
	    while(current != null){
		if(current.status.equals("Active") && current != client){ //Clients online
		    out.println(current.username);
		    count++;
		} else if(current.status.equals("Offline") && (System.currentTimeMillis() - current.timeout) < (LAST_HOUR * 1000) && current != client){//Clients online in the LAST_HOUR
		    out.println(current.username);
		    count++;
		}
		current = current.next;
	    }

	    //Prints that no one's been active recently
	    if(count == 0)
		out.println("No one's been online recently :(");
	}

	/*Displays message to all user when client enters broadcast command. 
	 * This includes the current client.*/
	public void broadcast(String message, User client) throws IOException{
	    User current = Users.head;
	    while(current != null){
		if(current.status.equals("Active") && current != client){
		    PrintWriter out = new PrintWriter(current.userSock.getOutputStream(), true);//Gets a stram to the recipient's client sock
		    out.println();
		    out.println(client.username + ": " + message);
		    out.println("Command: ");

		/* Outputting format must be different for the client to keep the
		 * nice outputting format in the client program */
		} else if(current.status.equals("Active") && current == client){
		    PrintWriter out = new PrintWriter(clntSock.getOutputStream(), true);
		    out.println(client.username + ": " + message);
		
		} else{ //Offline message functionality
		    current.messages.add(new Message(message, client.username, true));
		}
		current = current.next;
	    }
	}

	/* Private messages. Prevents client from sending messages to himself. Also
	 * will not send messages to users that have blocked this client.*/
	public void message(String message, String recipient, User client, PrintWriter out) throws IOException{
	    User userSendingTo = Users.find(recipient);

	    if(userSendingTo == null){ //person not found
		out.println("Recipient not found!");
	    
	    } else if(userSendingTo.isBlocked(client.username)){ //recipient has blocked the user
		out.println("You cannot send any message to " + recipient + ". You have been blocked by the user.");
	    
	    } else if(userSendingTo == client){ // Check for self messaging
		out.println("You cannot message yourself.");
	    
	    }else if(userSendingTo.status.equals("Active")){ //Send the messages
		PrintWriter clntOut = new PrintWriter(userSendingTo.userSock.getOutputStream(), true); //Open a stream to recipient's client socket
		clntOut.println();
		clntOut.println(client.username + ": " + message);
		clntOut.println("Command: ");
	    
	    } else { //Offline message functionality
		userSendingTo.messages.add(new Message(message, client.username, false));
	    }
	}

	/* Adds the specified user to the client's blocklist. Will persist until
	 * this user unblocks the user that was blocked. Client can still 
	 * message the blocked user. But not the other way around. Will still
	 * receive broadcasts from this user.*/
	public void block(String userToBlock, User client, PrintWriter out){
	    User block = Users.find(userToBlock);

	    if(block == null){ //non-existent user
		out.println("User doesn't exist");

	    } else if(block == client){ //No self-blocking
		out.println("Error! Can't block yourself!");
	    
	    } else if (client.isBlocked(userToBlock)){ //can't block twice
		out.println("User already blocked!");
	    
	    } else {//Success
	    	client.addToBlocks(userToBlock);
		out.println("You have successfully blocked " + userToBlock + " from sending you messages.");
	    }
	}

	/*Removes a user from the current user's blocklist*/
	public void unblock(String userToUnblock, User client, PrintWriter out){
	    
	    User unblock = Users.find(userToUnblock);

	    if(unblock == null){//non-existent user
		out.println("User doesn't exist!");
		
	    } else if(unblock == client){//can't unblock yourself
		out.println("Error: Can't unblock yourself!");

	    } else if (!client.isBlocked(userToUnblock)){//user isn't blocked
		out.println("You haven't blocked this user!");

	    } else {//Success
		client.removeFromBlocks(userToUnblock);
		out.println("You have successfully unblocked " + userToUnblock + ".");
	    }
	}	
	    
	/* Where the magic happens, within where...you get the point. The "main"
	 * function of a new client instance thread. Logs a user in, and then
	 * repeatedly reads input from the client socket and carries out the 
	 * client commands.*/
	public void run(){
	    
	    BufferedReader in = null;
	    PrintWriter out = null;
	    client = null;

	    try{
		
		/* Times out on a blocking read call after TIME_OUT seconds, sends
		 * a message to the client, and logs out*/
		clntSock.setSoTimeout((int)(TIME_OUT * 1000));

		//So we can read from and write to the socket
		in = new BufferedReader(new InputStreamReader(clntSock.getInputStream()));
	       	out = new PrintWriter(clntSock.getOutputStream(), true);
		Runtime.getRuntime().addShutdownHook(new Thread(new CntrlC(clntSock, out)));

		//We run login until we get a success.
		while((client = login(in, out)) == null);
		
		//Now that we've got a user, make sure it's known that he's online
		client.status = "Active";
		client.userSock = clntSock;

		//Print the messages user received while offline
		if(client.messages.head != null){
		    client.messages.printMessages(out);
		}


		StringBuffer clntBuff = new StringBuffer();
		String clntText;
		String clntCommand;
		String clntMess;
		
		/*Main loop that will read commands from the clntSock
		 * and carry out the client's commans*/
		out.println("Command: ");
		while((clntText = in.readLine()) != null){
		    clntBuff.append(clntText);
		    int i;

		    //Let's get the command
		    if((i = clntBuff.indexOf(" ")) != -1){
			clntCommand = clntBuff.substring(0, i);
		    } else {
			clntCommand = clntBuff.toString();
		    }

		    if(clntCommand.equals("whoelse"))
			whoelse(out, client);
		    
		    else if(clntCommand.equals("wholasthr"))
			wholasthr(out, client);
		    
		    else if(clntCommand.equals("broadcast")){
			
			//pull the actual message
			clntMess = clntBuff.substring(i + 1, clntBuff.length());
			broadcast(clntMess, client);
		    
		    } else if(clntCommand.equals("message")){
			
			//pull the actual message
			clntMess = clntBuff.substring(clntBuff.indexOf(" ", i + 1) + 1, clntBuff.length());
			String recipient = clntBuff.substring(i + 1, clntBuff.indexOf(" ", i + 1));
			message(clntMess, recipient, client, out);
		    
		    } else if(clntCommand.equals("block")){
			
			//pull the block target
			clntMess = clntBuff.substring(i + 1, clntBuff.length());
			block(clntMess, client, out);
		    
		    } else if(clntCommand.equals("unblock")){
			
			//pull the unblock target
			clntMess = clntBuff.substring(i + 1, clntBuff.length());
			unblock(clntMess, client, out);
		    
		    } else if(clntCommand.equals("logout")){
			out.println("Thank you for using Nick's chat program! See ya!");
			//make sure the client reads as logged out
			client.userSock = null;
			client.status = "Offline";
			client.timeout = System.currentTimeMillis();
			out.println(CLOSE_MSG);//message to terminate the program on the client side
			clntSock.close();//close the socket
			return;
		    } else if(clntCommand.equals("SIGINT")){//If client cntrl-c's after logging in successfully
			client.userSock = null;
			client.status = "Offline";
			client.timeout = System.currentTimeMillis();
			clntSock.close();
			return;
		    }
		    clntBuff.delete(0, clntBuff.length()); //reset for next loop
		    out.println("Command: ");
		}

		/* Make sure if the client was logged in, while the timeout
		 * occurred that the user was logged out properly. */
	    
	    } catch (SocketTimeoutException e){
		out.println();
		out.println("You've timed out!");
		if(client != null){
		    client.status = "Offline";
		    client.userSock = null;
		    client.timeout = System.currentTimeMillis();
		}
		out.println(CLOSE_MSG);
	    } catch (SocketException e){
		System.out.println("Exiting gracefully"); 
	    }catch(IOException e){
		System.out.println(e);
	    }
	}
    }

    /* This is used to catch Control-C and shutsdown each thread,
     * and makes the clients all close and terminate*/
    private static class CntrlC implements Runnable{
	
	public Socket clntSock;
	public PrintWriter out;

	public CntrlC(Socket clntSock, PrintWriter out){
	    this.clntSock = clntSock;
	    this.out = out;
	}

	public void run(){
	    try{
		out.println();
		out.println("Server shutdown");
	    	clntSock.close();
	    } catch(IOException e){
		return;
	    }
	}
    }
    /*This class defines a thread whose job it is to iterate through the blocked
     * list and occasionally remove users that have exceeded the time they
     * were to be blocked. */
    private static class blockCheck implements Runnable{
	
	public void run(){
	    User current = blocks.head;
	    try{
	    while(true){
		Thread.sleep(BLOCK_TIME * 1000);
		if(current == null){
		    current = blocks.head;
		    continue;
		}
		if((System.currentTimeMillis() - current.timeout) > (BLOCK_TIME * 1000)){
		    blocks.removeUser(current);
		}
		current = current.next;
	    }
	    } catch(InterruptedException e){
		System.out.println(e);
	    }
	}
    }


    public static void main(String[] args){

	if(args.length != 1){
	    System.out.println("Usage: <server_port>");
	    return;
	}

	loadUsers(); //loads from user_pass.txt

	(new Thread(new blockCheck())).start(); //thread that checks the blocks list

	ServerSocket servSock = null;

	try{
	    servSock = new ServerSocket(Integer.parseInt(args[0])); //creates server socket
	} catch(IOException e){
	    System.out.println(e);
	}

	Socket clntSock = null;
	try{
	    while(true){
	    	clntSock = servSock.accept(); //accepts clients
	    	(new Thread(new ClientInstance(clntSock, Users))).start();
	    }
	} catch(IOException e){
	    System.out.println(e);
	}
    }
}

