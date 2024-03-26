package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;

public class sprayAndWait_WithTransitivity_report extends Report implements MessageListener {
    // Map to store the creation times of messages
    private Map<String, Double> creationTimes;
    // List to store message latency
    private List<Double> latency;
    // List to store hop count of messages
    private List<Integer> hopCount;
    // List to store message buffer times
    private List<Double> msgBufferTimes;
    // List to store round trip times (RTT) of messages
    private List<Double> rtt;
    // Map To show coppies message
    private Map<String, Integer> numberOfCopies;

    // Variables to store various counts
    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    // Constructor
    public sprayAndWait_WithTransitivity_report() {
        init();
    }

    // Initialize method
    protected void init() {
        super.init();
        // Initialize data structures
        this.creationTimes = new HashMap<>();
        this.latency = new ArrayList<>();
        this.hopCount = new ArrayList<>();
        this.msgBufferTimes = new ArrayList<>();
        this.rtt = new ArrayList<>();
        this.numberOfCopies = new HashMap<>();

        // Initialize counts
        this.nrofDropped = 0;
        this.nrofRemoved = 0;
        this.nrofStarted = 0;
        this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;
    }

    // Method called when a message is deleted
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        // Check if message is a warmup message
        if (isWarmupID(m.getId())) {
            return;
        }
        // Increment dropped or removed count based on deletion reason
        if (dropped) {
            this.nrofDropped++;
        } else {
            this.nrofRemoved++;
        }
        // Calculate and store message buffer time
        this.msgBufferTimes.add(getSimTime() - m.getReceiveTime());
    }

    // Method called when a message transfer is aborted
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        // Check if message is a warmup message
        if (isWarmupID(m.getId())) {
            return;
        }
        // Increment aborted count
        this.nrofAborted++;
    }

    // Method called when a message is transferred
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        // Check if message is a warmup message
        if (isWarmupID(m.getId())) {
            return;
        }
        // Increment relayed count
        this.nrofRelayed++;
        // If message reaches the final target, calculate and store latency and hop
        // count
        if (finalTarget) {
            this.latency.add(getSimTime() - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCount.add(m.getHops().size() - 1);
        }
        // If message is a response, calculate and store round trip time (RTT)
        if (m.isResponse()) {
            this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
            this.nrofResponseDelivered++;
        }
    }

    // Method called when a new message is created
    public void newMessage(Message m) {
        // Check if simulation is in the warmup phase
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }
        // Store message creation time
        this.creationTimes.put(m.getId(), getSimTime());

        // Increment created count
        this.nrofCreated++;
        // If message is a response, increment response request created count
        if (m.getResponseSize() > 0) {
            this.nrofResponseReqCreated++;
        }
    }

    // Method called when a message transfer is started
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        // Check if message is a warmup message
        if (isWarmupID(m.getId())) {
            return;
        }
        // Increment started count
        this.nrofStarted++;
    }

    // Method called when the simulation is done
    @Override
    public void done() {
        // Write report statistics
        write("My Report Of Spray And Wait Routing " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));

        // Calculate delivery probability
        double deliveryProb = (this.nrofCreated > 0) ? (1.0 * this.nrofDelivered) / this.nrofCreated : 0;

        // Format report statistics
        String statsText = "created: " + this.nrofCreated +
                "\nstarted: " + this.nrofStarted +
                "\nrelayed: " + this.nrofRelayed +
                "\ndropped: " + this.nrofDropped +
                "\ndelivered: " + this.nrofDelivered +
                "\ndelivery_prob: " + format(deliveryProb); // Include delivery probability here

        // Write formatted statistics to the report
        write(statsText);
        // Call the superclass done() method
        super.done();
    }

}
