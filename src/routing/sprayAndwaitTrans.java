package routing;

import java.util.ArrayList;
import java.util.List;

import core.DTNHost;
import core.Connection;
import core.Message;
import core.Settings;

public class sprayAndwaitTrans extends ActiveRouter {
    /** Inisiasi Variable Final */
    public static final String numberOfCopies = "numberofcopies";
    public static final String binaryMode = "binaryMode";
    public static final String SprayAndWait_nm = "SprayAndWaitRouter"; // spray And wait Name Space
    public static final String msg_count_property = SprayAndWait_nm + "." + "copies";

    public int initalNumberOfCopies;
    public boolean isBinary;

    /** Make the Constructor */
    public sprayAndwaitTrans(Settings setting) {
        super(setting);

        // make an object for setting
        Settings SprayWaitSet = new Settings(SprayAndWait_nm);

        // make the initialNumberOfCopies
        initalNumberOfCopies = SprayWaitSet.getInt(numberOfCopies);
        isBinary = SprayWaitSet.getBoolean(binaryMode);
    }

    /**
     * make the Copy Constructor
     * 
     * @param snw
     */
    protected sprayAndwaitTrans(sprayAndwaitTrans snw) {
        super(snw);

        initalNumberOfCopies = snw.initalNumberOfCopies;
        isBinary = snw.isBinary;
    }


    /* Make The Receving Message */
    @Override
    public int receiveMessage(Message msg, DTNHost from) {
        return super.receiveMessage(msg, from);
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message msg = super.messageTransferred(id, from);
        Integer numberOfCopies = (Integer) msg.getProperty(msg_count_property);

        assert numberOfCopies != null : "is Not Spray And Wait Message" + msg;

        // Check if the copies is goes to binary or not
        if (isBinary) {
            numberOfCopies = (int) Math.floor(numberOfCopies / 2.0);
        } else {
            // the standard code with single copy of msg
            numberOfCopies = 1;
        }
        msg.updateProperty(msg_count_property, numberOfCopies);

        return msg;

    }

    @Override
    public boolean createNewMessage(Message msg) {
        makeRoomForMessage(msg.getSize());

        msg.setTtl(this.msgTtl);
        msg.addProperty(msg_count_property, new Integer(initalNumberOfCopies));
        addToMessages(msg, true);
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (!canStartTransfer() || isTransferring()) {
            return;
        }

        if (exchangeDeliverableMessages() != null) {
            return;
        }

        @SuppressWarnings(value = "unchecked")
        List<Message> coppiesLeft = sortByQueueMode(getMessagesWithCopiesLeft());

        if (coppiesLeft.size() > 0) {
            this.tryMessagesToConnections(coppiesLeft, getConnections());
        }
    }

    /**
     * Creates and returns a list of messages this router is currently
     * carrying and still has copies left to distribute (nrof copies > 1).
     * 
     */
    protected List<Message> getMessagesWithCopiesLeft() {
        List<Message> list = new ArrayList<Message>();

        // create loop for Message
        for (Message msg : getMessageCollection()) {
            Integer numberOfCopies = (Integer) msg.getProperty(msg_count_property);
            assert numberOfCopies != null : "spray And Wait " + msg +
                    "didn't have " + "Number of copies";
            if (numberOfCopies > 1) {
                list.add(msg);
            }
        }
        return list;

    }

    /**
     * Called just before a transfer is finalized (by
     * {@link ActiveRouter#update()}).
     * Reduces the number of copies we have left for a message.
     * In binary Spray and Wait, sending host is left with floor(n/2) copies,
     * but in standard mode, nrof copies left is reduced by one.
     */
    @Override
    protected void transferDone(Connection con) {
        Integer numberOfCopies;
        String msgId = con.getMessage().getId();

        /* get this router's copy of the message */
        Message msg = getMessage(msgId);

        if (msg == null) { // message has been dropped from the buffer after..
            return; // ..start of transfer -> no need to reduce amount of copies
        }

        /* reduce the amount of copies left */
        numberOfCopies = (Integer) msg.getProperty(msg_count_property);

        if (isBinary) {
            numberOfCopies = (int) Math.floor(numberOfCopies / 2.0);
        } else {
            numberOfCopies--;
        }

        msg.updateProperty(msg_count_property, numberOfCopies);

    }

    @Override
    public sprayAndwaitTrans replicate() {
        return new sprayAndwaitTrans(this);
    }

    /**
     * Updates transitive (A->B->C) delivery predictions.
     * <CODE>P(a,c) = P(a,c)_old + (1 - P(a,c)_old) * P(a,b) * P(b,c) * BETA
     * </CODE>
     * 
     * @param host The B host who we just met
     */
    private void updateTransitivePreds(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof sprayAndwaitTrans : "Spray And Wait Work" +
            " With other routers of same type";

            
        
    }

}