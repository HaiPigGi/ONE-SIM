package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;

public class DropPerIntervalWithBuffOcV2 extends Report implements MessageListener, UpdateListener {
     // Variable to store dropped messages count per host within an interval
     private int droppedMessagesCount;

    // Map to store dropped messages count per host within an interval
    private Map<DTNHost, Integer> droppedMessagesPerInterval;

    // Interval for recording dropped messages
    private int interval;

    // Last recorded time
    private double lastRecord;

    /**
     * Constructor to initialize the class.
     */
    public DropPerIntervalWithBuffOcV2() {
        // Initialize data structures
        init();

        // Set the interval to 300 (5 seconds)
        this.interval = 300;

        // Initialize lastRecord to an invalid value
        this.lastRecord = -1;

       // Initialize droppedMessagesCount
       this.droppedMessagesCount = 0;
        this.droppedMessagesPerInterval = new HashMap<>();
    }

    /**
     * Initialize method to set up data structures.
     */
    protected void init() {
        // Call superclass init method
        super.init();
    }

    /**
     * Method to handle deleted messages.
     * Checks if the message ID is part of the warm-up period.
     * Increments the count of dropped messages per host within the interval.
     * 
     * @param m       The message deleted.
     * @param where   The host where the message was deleted.
     * @param dropped Boolean indicating whether the message was dropped.
     */
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        // Check if the message ID is part of the warm-up period
        if (isWarmupID(m.getId())) {
            return;
        }

        // If the message is dropped
        if (dropped) {
            // Increment the count of dropped messages per host within the interval
            if (!droppedMessagesPerInterval.containsKey(where)) {
                droppedMessagesPerInterval.put(where, 1);
            } else {
                int totalInterval = droppedMessagesPerInterval.get(where);
                droppedMessagesPerInterval.put(where, ++totalInterval);
            }
        }
    }

    // MessageListener methods to handle other message events
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
    }

    public void newMessage(Message m) {
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    /**
     * Updates dropped messages at specified intervals.
     * This method checks if the specified interval has elapsed
     * since the last update, and if so, it triggers the recording
     * of dropped messages using the recordDroppedMessages method.
     *
     * @param hosts List of DTNHosts representing the hosts in the network.
     */
    public void updated(List<DTNHost> hosts) {
        // Get the current simulation time
        double currentTime = SimClock.getTime();

        // Check if the interval has elapsed since the last update
        if (currentTime - lastRecord >= interval) {
            // Update the lastRecord time to the current time
            lastRecord = currentTime;

            // Trigger the recording of dropped messages
            recordDroppedMessages();
        }
    }

    /**
     * Records dropped messages per host for the last interval.
     * This method iterates through the droppedMessagesPerInterval map,
     * retrieves dropped message counts per host, and updates the
     * droppedMessagesByHost map accordingly. It also clears the
     * droppedMessagesPerInterval map after processing.
     */
    private void recordDroppedMessages() {
      droppedMessagesPerInterval.put(null, droppedMessagesCount);
        System.out.println(droppedMessagesCount);
       droppedMessagesCount=0;
    }

    @Override
    public void done() {
        StringBuilder output = new StringBuilder();
        // Append the dropped messages count per host within the last interval
        for (Map.Entry<DTNHost, Integer> entry : droppedMessagesPerInterval.entrySet()) {
            DTNHost host = entry.getKey();
            int droppedMessagesInterval = entry.getValue();
            output.append("Host: ").append(host).append(" Dropped Messages: ").append(droppedMessagesInterval).append("\n");
        }
        
        // Write the output
        write(output.toString());
        
        super.done();
    }
    
    
}
