/*Class responsible for the maintenance of the list of
 * possible users that can login to the program. Finds users
 * in the list, adds them to the list, etc. */

import java.net.*;

public class UserList{

    public User head;
    public User tail;

    public UserList(){
	head = null;
	tail = null;
    }

    //Adds users to a list, used to load the users initially
    public void add(String username, String password){
	User userToAdd = new User(username, password);
	if(head == null){
	    head = userToAdd;
	    tail = userToAdd;
	    return;
	}
	tail.next = userToAdd;
	userToAdd.next = null;
	tail = userToAdd;
    }

    /*In the server program, there is a special UserList
     * that maintains those IP/user combinations that have 
     * been blocked by the server for failed logins. They are added
     * to the list as full users. This method is called to do so. */
    public void addBlock(User userToAdd){
	if(head == null){
	    head = userToAdd;
	    tail = userToAdd;
	    return;
	}
	tail.next = userToAdd;
	userToAdd.next = null;
	tail = userToAdd;
    }

    /*Returns the user with specified username, null if nonexistent*/
    public User find(String usernameToFind){
	if(head == null){
	    return null;
	}
	User current = head;
	while(current != null){
	    if(current.username.equals(usernameToFind)){
		return current;
	    }
	    current = current.next;
	}
	return null;
    }

    /*Removes user from the list, used on the block list mentioned earlier*/
    public void removeUser(User userToRemove){
	if(head == userToRemove && userToRemove.next == null){
	    head = null;
	    tail = null;
	    return;
	}
	User current = head;
	while(current.next != userToRemove){
	    current = current.next;
	}
	current.next = userToRemove.next;
    }

    /* Prints all the users in a list and their timeouts. Used
     * for debugging */
    public void printAll(){
	User current = head;
	while(current != null){
	    System.out.print(current.username + " " + current.password + " " + (System.currentTimeMillis() - current.timeout) + "\n");
	    current = current.next;
	}
    }
}
