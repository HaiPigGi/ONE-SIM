package report.myReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.Report;

public class EpidemicRoutingReport extends Report implements MessageListener {

    // Map to store message creation times
    private Map<String, Double> creationTimes;
    // Lists to store latency, hop count, message buffer times, and round trip times
    private List<Double> latency;
    private List<Integer> hopCount;
    private List<Double> msgBufferTimes;
    private List<Double> rtt; // round Trip Times

    // Counters for various message events
    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    /*
     * Constructor
     */
    public EpidemicRoutingReport() {
        init();
    }

    // Initialize method to set up data structures
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.latency = new ArrayList<Double>();
        this.hopCount = new ArrayList<Integer>();
        this.msgBufferTimes = new ArrayList<Double>();
        this.rtt = new ArrayList<Double>();

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

    // MessageListener method to handle deleted messages
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }
        if (dropped) {
            this.nrofDropped++;
        }
    }

    // MessageListener method to handle aborted message transfers
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofAborted++;
    }

    // MessageListener method to handle transferred messages
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofRelayed++;
        if (finalTarget) {
            this.latency.add(getSimTime() - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCount.add(m.getHops().size() - 1);
        }
        if (m.isResponse()) {
            this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
            this.nrofResponseDelivered++;
        }
    }

    // MessageListener method to handle new messages
    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }
        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        if (m.getResponseSize() > 0) {
            this.nrofResponseReqCreated++;
        }
    }

    // MessageListener method to handle started message transfers
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofStarted++;
    }

    // Method called when the simulation is done to generate the report
    @Override
    public void done() {
        write("My Report Of Epidemic Routing for some scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));
        double deliveryProb = 0; // delivery probability
        double responseProb = 0; // request-response success probability
        double overHead = Double.NaN; // overhead ratio

        // Calculate delivery probability
        if (this.nrofCreated > 0) {
            deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
        }
        // Calculate overhead ratio
        if (this.nrofDelivered > 0) {
            overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) / this.nrofDelivered;
        }
        // Calculate request-response success probability
        if (this.nrofResponseReqCreated > 0) {
            responseProb = (1.0 * this.nrofResponseDelivered) / this.nrofResponseReqCreated;
        }

        // Construct the report text
        String statsText = "created: " + this.nrofCreated +
                "\nstarted: " + this.nrofStarted +
                "\nrelayed: " + this.nrofRelayed +
                "\naborted: " + this.nrofAborted +
                "\ndropped: " + this.nrofDropped +
                "\nrtt_med: " + getMedian(this.rtt);

        // Write the report text
        write(statsText);
        super.done();
    }

}
