import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

	private ArrayBlockingQueue<Message> spaceExplorerChannel = new ArrayBlockingQueue<Message>(100);
	private ArrayBlockingQueue<Message> headQuarterChannel = new ArrayBlockingQueue<Message>(100);
	private boolean gotFirstMessage = false;
	private String firstThreadIn = "";
	private Message messageToSend = null;
	private ReentrantLock re = new ReentrantLock();
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message) {
		try {
			spaceExplorerChannel.put(message);
//			System.out.println("[SE Channel]: "
//					+ message.getParentSolarSystem() + " "
//					+ message.getCurrentSolarSystem() + " "
//					+ message.getData());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {
		Message m = null;
		//System.out.println("in get message space explorer");
		try {
			m = spaceExplorerChannel.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m;
	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {

		re.lock();
		firstThreadIn = Thread.currentThread().getName();
		try {
			if (message.getData().matches("END")) {
				//System.out.println("Am primit END, nu l-am bagat in coada\n");
				return;
			}

			if (message.getData().matches("EXIT")) {
				headQuarterChannel.put(message);
			}

			if (!gotFirstMessage) {
				messageToSend = new Message(-1, null);
				messageToSend.setParentSolarSystem(message.getCurrentSolarSystem());
				gotFirstMessage = true;
			} else if (firstThreadIn.matches(Thread.currentThread().getName())){
				messageToSend.setCurrentSolarSystem(message.getCurrentSolarSystem());
				messageToSend.setData(message.getData());
//				System.out.println("[HQ Channel]: "
//						+ messageToSend.getParentSolarSystem() + " "
//						+ messageToSend.getCurrentSolarSystem() + " "
//						+ messageToSend.getData());

				// putting message in queue
				headQuarterChannel.put(messageToSend);

				// reseting
				gotFirstMessage = false;
				messageToSend = null;
				firstThreadIn = "";
				re.unlock();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {
		Message m = null;
		try {
			//System.out.println("in get message hq channel");
			m = headQuarterChannel.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m;
	}
}
