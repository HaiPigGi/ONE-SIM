package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;

public class myReport extends Report implements MessageListener {

    private Map<String, Double> creationTimes;
    private List<Double> latency;
    private List<Integer> hopCount;
    private List<Double> msgBufferTimes;
    private List<Double> rtt; // round Trip Times

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
     * create Consructor
     * 
     */
    public myReport() {
        init();
    }

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

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }
        if (dropped) {
            this.nrofDropped++;
        } else {
            this.nrofRemoved++;
        }

        this.msgBufferTimes.add(getSimTime() - m.getReceiveTime());
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofAborted++;
    }
    public void messageTransferred (Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofRelayed++;
        if (finalTarget) {
            this.latency.add(getSimTime()-this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCount.add(m.getHops().size()-1);
        }
        if (m.isResponse()) {
            this.rtt.add(getSimTime()-m.getRequest().getCreationTime());
            this.nrofResponseDelivered++;
        }
    }

    public void newMessage (Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }
        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        if (m.getResponseSize()>0) {
            this.nrofResponseReqCreated++;
        }
    }
    public void messageTransferStarted (Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofStarted++;
    } 

    @Override
    public void done() {
        write("My Report Of Epidemic Routing for some scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));
        double deliveryProb = 0; // delivery probability
        double responseProb = 0; // request-response success probability
        double overHead = Double.NaN; // overhead ratio

        if (this.nrofCreated > 0) {
            deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
        }
        if (this.nrofDelivered > 0) {
            overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) /
                    this.nrofDelivered;
        }
        if (this.nrofResponseReqCreated > 0) {
            responseProb = (1.0 * this.nrofResponseDelivered) /
                    this.nrofResponseReqCreated;
        }

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

        write(statsText);
        super.done();
    }

}
