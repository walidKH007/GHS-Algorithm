import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class MessageBus {
	
	public static final int MAX_SIZE = 100;

	private List<Message> messageStore = new ArrayList<Message>();	

	public void put(Message message){
		
		messageStore.add(message);
	
	}
	
	public Message getMessage(int nodeId){
		
		for (Message m : messageStore) {	
			if(m.getTo() == nodeId) {
				Message message = m;
				messageStore.remove(m);
				return message;
			}
		}

		return null;
	}
	
	
}
