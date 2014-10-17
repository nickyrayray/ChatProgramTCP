JCC = javac

JFLAGS = -g

default: Server.class Client.class UserList.class User.class Message.class MessageList.class

Server.class: Server.java
	$(JCC) $(JFLAGS) Server.java

Client.class: Client.java
	$(JCC) $(JFLAGS) Client.java

UserList.class: UserList.java
	$(JCC) $(JFLAGS) UserList.java

User.class: User.java
	$(JCC) $(JFLAGS) User.java

Message.class: Message.java
	$(JCC) $(JFLAGS) Message.java

MessageList.class: MessageList.java
	$(JCC) $(JFLAGS) MessageList.java

.PHONY: clean
clean: 
	rm -f *.class
