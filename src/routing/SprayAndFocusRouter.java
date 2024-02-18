package routing;

import java.util.ArrayList;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;

public class SprayAndFocusRouter extends ActiveRouter {
    // Constants and properties
    public static final String NUMBER_OF_COPIES = "number_of_copies";
    public static final String SPRAY_AND_FOCUS_NS = "SprayAndFocusRouter";
    public static final String MSG_COUNT_PROPERTY = SPRAY_AND_FOCUS_NS + "." + "copies";

    // Variables
    public int initialNumberOfCopies;

    // Constructor
    public SprayAndFocusRouter(Settings settings) {
        super(settings);
        Settings sprayAndFocusSettings = new Settings(SPRAY_AND_FOCUS_NS);
        initialNumberOfCopies = sprayAndFocusSettings.getInt(NUMBER_OF_COPIES);
    }

    // Copy constructor
    protected SprayAndFocusRouter(SprayAndFocusRouter snw) {
        super(snw);
        this.initialNumberOfCopies = snw.initialNumberOfCopies;
    }

    // Receive message method
    @Override
    public int receiveMessage(Message msg, DTNHost from) {
        return super.receiveMessage(msg, from);
    }

    // Message transferred method
    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message msg = super.messageTransferred(id, from);
        adjustNumberOfCopies(msg);
        return msg;
    }

    // Create new message method
    @Override
    public boolean createNewMessage(Message msg) {
        makeRoomForMessage(msg.getSize());
        msg.setTtl(this.msgTtl);

        // Check if the property already exists in the message
        if (msg.getProperty(MSG_COUNT_PROPERTY) == null) {
            msg.addProperty(MSG_COUNT_PROPERTY, initialNumberOfCopies);
        }
        addToMessages(msg, true);
        return true;
    }

    // Update method
    @Override
    public void update() {
        super.update();

        if (!canStartTransfer() || isTransferring()) {
            return;
        }

        if (exchangeDeliverableMessages() != null) {
            return;
        }

        List<Message> copiesLeft = getMessagesWithCopiesLeft();

        if (!copiesLeft.isEmpty()) {
            tryMessagesToConnections(copiesLeft, getConnections());
        }

        // check for the fase focus
        for (Message msg : getMessageCollection()) {
            Integer numberOfCopies = (Integer) msg.getProperty(MSG_COUNT_PROPERTY);

            if (numberOfCopies != null && numberOfCopies <= 1) {
                // send message to destination
                sendMessageToDestination(msg);
            }
        }
    }

    // Method to send Message to Destination
    private void sendMessageToDestination(Message msg) {
        // Get Node Destination
        DTNHost destination = msg.getTo();

        // Get message id
        String messageId = msg.getId();

        // Log the message ID
        System.out.println("Sending message " + messageId + " to destination node " + destination);

        // Log the message ID and other relevant information
        System.out.println("Sending message " + messageId + " from node " + this.getHost() + " to destination node "
                + destination);

        // Get Router from node destination
        MessageRouter router = destination.getRouter();

        // Send Message with Router to destination node
        router.sendMessage(messageId, destination);
    }

    // Method to adjust the number of copies of a message
    private void adjustNumberOfCopies(Message msg) {
        Integer numberOfCopies = (Integer) msg.getProperty(MSG_COUNT_PROPERTY);
        if (numberOfCopies != null && numberOfCopies > 1) {
            // Reduce the number of copies by half
            numberOfCopies /= 2;
            // Update the existing property instead of adding it again
            msg.addProperty(MSG_COUNT_PROPERTY, numberOfCopies);
        }
    }

    // Get messages with copies left method
    protected List<Message> getMessagesWithCopiesLeft() {
        List<Message> list = new ArrayList<>();

        for (Message msg : getMessageCollection()) {
            Integer numberOfCopies = (Integer) msg.getProperty(MSG_COUNT_PROPERTY);
            assert numberOfCopies != null : "Spray and Focus message " + msg + " didn't have number of copies property";
            if (numberOfCopies > 1) {
                list.add(msg);
            }
        }

        return list;
    }

    // Transfer done method
    @Override
    protected void transferDone(Connection con) {
        Message msg = getMessage(con.getMessage().getId());
        if (msg != null) {
            adjustNumberOfCopies(msg);
        }
    }

    // Replicate method
    @Override
    public SprayAndFocusRouter replicate() {
        return new SprayAndFocusRouter(this);
    }
}
