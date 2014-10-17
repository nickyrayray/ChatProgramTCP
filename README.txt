Nicholas Falba
Short Chat Program

a) My code has 6 public classes, Server.java, Client.java, which house the two main methods
for Server and Client, respectively. Also User.java, UserList.java, which support methods 
involving maintenance of the list of users registered with the program that are read in from the user_pass.txt file.
And finally, MessageList.java and Message.java which support the functions involved with adding,
storing, and printing messages that support offline messaging. These classes and methods
are described in detail in the comments on their pages.

In summary:

Server: Initializes the client list and the server socket and runs in an infinite while
loop accepting clients as connections are made. For each new client, the server opens
a new socket and makes a new thread for the client, described by ClientInstance class
defined in Server.java. There, the methods that the server must support are defined. Also
the server supports exiting the clients when Cntrl-C is pushed. 

Client: The client works by running an infinite loop that reads from standard in in the 
main thread. It also has a thread whose job is to print to stdout what it reads from the
socket. So the client inputs something to the Socket, the server gets it, interprets it,
and then sends some output, which is read from the socket on the client side. The thread
that reads from the socket prints to stdout in the client's terminal. The client program
ends in one of three ways. It receives CLOSE_MSG, which is a string indicating that the
client should close the socket on its end and terminate. This happens after the client 
enters "logout", or it times out. Or the client could press cntrl-c, in which case, 
there is a thread defined by the Cntrl-C class to handle interrupts. Then, if the server
cntrl-c's in which case a special server-shutdown message is sent and the client 
terminates. All of these terminations end through the Cntrl-C thread in different ways,
even the regular logout way, since there was no way to unblock the stdin.readLine() from
the main thread, otherwise.

 More specifically:

When the client connects, it is asked to login. Once it logs in, it is shown its offline
messages in the order they were sent to him while offline. Then the server sends the 
clients all the messages that were sent to him while he was offline, if any. Then
the user is asked to enter commands to invoke different functionalities of the chat
program.

One thing: This is the first time I've had to create a multi-threaded program. I am just
learning about threading in operating systems, so my threading might be a bit sloppy,
although everything works. While no Users should be able to modify shared fields at the
same time, I may have overlooked something that would be obvious to an experienced
multi-threaded programmer. Since this isn't OS, I hope the TAs would be lenient to
any unforseen issues related to multi-threading.

b)I developed all my code in vim, run from Terminal on my mac. It was a pain.

c) To run my code, you must go into the folder when all the .java files are stored. It is
the folder that comes when you  unzip the file, ChatProgHw1 and just type
"make". Then run the server and client from the command line as in the java examples on
the assignment instructions.

d) java Server 48642
java Client localhost 48642 are examples to compile my code

In the server you don't type anything. It just facilitates commands between clients.
In Client:

whoelse -> show everyone currently online
wholasthr -> show everyone online in last hr
broadcast Hey guys -> User: hey guys appears in every online terminal
logout -> logs user out

are some examples.

e) I included broadcast messages to be included in the offline message functionality. So 
when a user logs in, it will immediately shown both its private messages and public 
broadcasts while it was offline. The broadcasts are clearly marked. 
