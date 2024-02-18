package report.myReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.Report;

public class sprayAndFocusReport extends Report implements MessageListener {
    // Map to store creation times of messages
    private Map<String, Double> creationTimes;
    // List to store message latency
    private List<Double> latency;
    // List to store hop count of messages
    private List<Integer> hopCount;
    // List to store message buffer times
    private List<Double> msgBufferTimes;
    // List to store round trip times (RTT) of messages
    private List<Double> rtt;

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
    public sprayAndFocusReport() {
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
        // If message reaches final target, calculate and store latency and hop count
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
        // Check if simulation is in warmup phase
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
        write("My Report Of Spray And Focus Routing for some scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));
        // Calculate delivery probability, response probability, and overhead ratio
        double deliveryProb = (this.nrofCreated > 0) ? (1.0 * this.nrofDelivered) / this.nrofCreated : 0;
        double responseProb = (this.nrofResponseReqCreated > 0) ? (1.0 * this.nrofResponseDelivered) / this.nrofResponseReqCreated : 0;
        double overHead = (this.nrofDelivered > 0) ? (1.0 * (this.nrofRelayed - this.nrofDelivered)) / this.nrofDelivered : Double.NaN;

        // Format report statistics
        String statsText = "created: " + this.nrofCreated +
                "\nstarted: " + this.nrofStarted +
                "\nrelayed: " + this.nrofRelayed +
                "\naborted: " + this.nrofAborted +
                "\ndropped: " + this.nrofDropped +
                "\nremoved: " + this.nrofRemoved +
                "\ndelivered: " + this.nrofDelivered +
                "\ndelivery_prob: " + format(deliveryProb) +
                "\nresponse_prob: " + format(responseProb) +
                "\noverhead_ratio: " + format(overHead) +
                "\nlatency_avg: " + getAverage(this.latency) +
                "\nlatency_med: " + getMedian(this.latency) +
                "\nhopcount_avg: " + getIntAverage(this.hopCount) +
                "\nhopcount_med: " + getIntMedian(this.hopCount) +
                "\nbuffertime_avg: " + getAverage(this.msgBufferTimes) +
                "\nbuffertime_med: " + getMedian(this.msgBufferTimes) +
                "\nrtt_avg: " + getAverage(this.rtt) +
                "\nrtt_med: " + getMedian(this.rtt);

        // Write formatted statistics to report
        write(statsText);
        // Call superclass done() method
        super.done();
    }
}
