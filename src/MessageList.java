/*Class is the list of messages sent to a user by other
 * users while he or she was offline. This class supports adding
 * messages to the list and then reading them all out, and emptying
 * the list.*/

import java.io.*;

public class MessageList{
    
    public Message head;
    public Message tail;

    public void add(Message messageToAdd){
	if(head == null){
	    head = messageToAdd;
	    tail = messageToAdd;
	    return;
	}
	tail.next = messageToAdd;
	messageToAdd.next = null;
	tail = messageToAdd;
    }

    public void printMessages(PrintWriter out){
	out.println("Messages while you were offline: ");
	while(head != null){
	    if(head.isBroadcast){
		out.println("(broadcast message) " + head.sender + ": " + head.message);
	    } else {
		out.println(head.sender + ": " + head.message);
	    }
    	    head = head.next;
	}
	tail = null;
    }
}
