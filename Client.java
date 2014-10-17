/*Client program that supports the client side of the chat program
 * It consists of two threads. One thread is only used to input commands.
 * The other thread reads output from the Socket to the screen.
 */

import java.io.*;
import java.net.*;

public class Client{

    /*When this string is received from the Server, the Client is to close the 
     * Socket and exit immediately. Can be generated from the Client typing "logout"
     * or from a timeout
     */
    private static final String CLOSE_MSG = "CLOSESOCK";
    
    private static boolean isOpen = true; //Becomes false when Server sends CLOSE_MSG
    
    private static boolean serverShutdown = false;

    /*Class defining the thread responsible for reading from the socket 
     * and outputing the result to stdout. It is also responsible for 
     * closing the socket when the time comes*/
    private static class Receiver implements Runnable{
	
	public Socket clntSock;
	public BufferedReader stdIn, in;

	public Receiver(Socket clntSock, BufferedReader stdIn, BufferedReader in){
	    this.clntSock = clntSock;
	    this.stdIn = stdIn;
	    this.in = in;
	}

	public void run(){
	    try{
		String servOut;
		while((servOut = in.readLine()) != null){
		    if(servOut.equals("Server shutdown")){
			System.out.println(servOut);
			serverShutdown = true;
			System.exit(1);
		    }
		    if(servOut.equals(CLOSE_MSG)){
			isOpen = false;
			System.exit(1);//Client is blocked on a read from StdIn
		       		      // but program has to terminate	
		    }

		    /*Formatting so that certain server messages get client responses
		     * on the same line */
		    if(servOut.equals("Enter username: ") || servOut.equals("Enter password: ") || servOut.equals("Command: "))
			System.out.print(servOut);
		    else
			System.out.println(servOut);
		}
	    } catch(IOException e){
		return;
	    }
	}
    }

    /*This will run no matter how the client program is shutdown. If a regular
     * logout cammand is used, isOpen is false; if the server fails, 
     * serverShutdown is true, and if SigInt is used, isOpen is true
     * and the shutdowns follow these conditions.*/
    private static class CntrlC implements Runnable{
	
	public Socket clntSock;
	public PrintWriter out;
	public BufferedReader in;

	public CntrlC(Socket clntSock, PrintWriter out, BufferedReader in){
	    this.clntSock = clntSock;
	    this.out = out;
	    this.in = in;
	}

	public void run(){
	    try{
		if(serverShutdown){
		    clntSock.close();
		    return;
		}
		if(isOpen){
		    out.println("SIGINT");
		    clntSock.close();
		} else {
		    clntSock.close();
		}
	    } catch (IOException e){
		return;
	    }
	}
    }

    public static void main(String[] args){
	if(args.length != 2){
	    System.out.println("Usage: <machine_name> <listening_port>");
	    return;
	}

	Socket clntSock = null;
	String successMessage = "Successfully logged in! Welcome to Nick's chat program!";
	try{
	    clntSock = new Socket(args[0], Integer.parseInt(args[1]));
	    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	    PrintWriter out = new PrintWriter(clntSock.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(new InputStreamReader(clntSock.getInputStream()));

	    Runtime.getRuntime().addShutdownHook(new Thread(new CntrlC(clntSock, out, in)));
	    (new Thread(new Receiver(clntSock, stdIn, in))).start();
	    String command;
	    while(isOpen){ //While the socket is open
		command = stdIn.readLine();//Read client command from StdIn
		out.println(command);//Write command to the socket
	    }
	} catch(IOException e){
	    System.out.println("Exception caught!");
	}
    }
}

