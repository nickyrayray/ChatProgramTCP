
public class threadcreation{

    private static class threadMessage implements Runnable{

	public void run(){
	    System.out.println("This is thread " + Thread.currentThread().getName());
	}
    }

    
    public static void main(String[] args){
	int i = 0;
	while(i < 5){
	    (new Thread(new threadMessage())).start();
	    i++;
	}
    }
}
