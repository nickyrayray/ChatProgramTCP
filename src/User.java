/*Class handles all the mainenance associated with the User. It
 * also supports handling a particular User's block list, checking
 * to see if a particular user has been blocked by the current user,
 * clocking Users out, etc.*/

import java.net.*;
import java.io.*;

public class User{

    public String username;
    public String password;
    public Socket userSock;
    public String status; //On or offline
    public long timeout; //Time when last logged out
    public String[] blockList; //List of users blocked by current user
    public MessageList messages; //Messages received while offline
    public User next; //Part of a linked list

    public User(Socket clntSock){
	userSock = clntSock;
	status = "Offline";
	blockList = new String[10];
	messages = new MessageList();
    }

    //Used to initially load users
    public User(String username, String password){
	this.username = username;
	this.password = password;
	userSock = null;
	status = "Offline";
	messages = new MessageList();
	blockList = new String[10];
    }

    //Adds a user's name to the list of blocks made by the current user
    public void addToBlocks(String nameToAdd){
	if(blockList[blockList.length - 1] != null){
	    String[] newBList = new String[blockList.length + 10];
	    for(int i = 0; i < blockList.length; i++){
		newBList[i] = blockList[i];
	    }
	    blockList = newBList;
	}
	int next;
	for(next = 0; next < blockList.length; next++){
	    if(blockList[next] == null)
		break;
	}
	blockList[next] = nameToAdd;
    }

    //Removes a user's name from the current user's block list
    public void removeFromBlocks(String nameToRemove){
	int index = 0;
	for(int i = 0; i < blockList.length; i++){
	    if(blockList[i].equals(nameToRemove)){
		index = i;
		break;
	    }
	}
	for(int j = index; blockList[j] != null; j++){
	    blockList[j] = blockList[j + 1];
	}
    }

    //Checks to see if a particular user is blocked
    public boolean isBlocked(String nameToFind){
	for(int i = 0; i < blockList.length; i++){
	    if(blockList[i] == null)
		break;
	    if(blockList[i].equals(nameToFind))
		return true;
	}
	return false;
    }

}

