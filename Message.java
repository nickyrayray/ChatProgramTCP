/*Class that supports the offline meesage function by housing
 * the sender and message text from a user for a later user
 * when he/she comes online*/

public class Message{

    public String message; 
    public String sender;
    public boolean isBroadcast; //To see if the offline message was a broadcast or private message
    public Message next;

    public Message(String message, String sender, boolean isBroadcast){
	this.message = message;
	this.sender = sender;
	this.isBroadcast = isBroadcast;
    }

}
