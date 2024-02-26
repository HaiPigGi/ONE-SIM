package report.myReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.Report;

public class TotalDroppedReport_versi1 extends Report implements MessageListener {
    private Map<DTNHost, Integer> droppedMessagesByHost;

    // Counters for various message events
    private int nrofDropped;

    /*
     * Constructor
     */
    public TotalDroppedReport_versi1() {
        init();
        this.droppedMessagesByHost = new HashMap<>();
    }

    // Initialize method to set up data structures
    protected void init() {
        super.init();
        this.nrofDropped = 0;

    }

    // MessageListener method to handle deleted messages
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (dropped) {
            droppedMessagesByHost.put(where, droppedMessagesByHost.getOrDefault(where, 0) + 1);
        }

    }

    // // Method to get the total number of dropped messages for a specific host
    // public int getDroppedMessagesForHost(DTNHost host) {
    //     return droppedMessagesByHost.getOrDefault(host, 0);
    // }

    // MessageListener method to handle aborted message transfers
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {

    }

    // MessageListener method to handle transferred messages
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {

    }

    // MessageListener method to handle new messages
    public void newMessage(Message m) {

    }

    // MessageListener method to handle started message transfers
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {

    }

    // Method called when the simulation is done to generate the report
    @Override
    public void done() {
        // Write the report
        int total =0;
        for (DTNHost host : droppedMessagesByHost.keySet()) {
            write("Node : " + host + ": " + droppedMessagesByHost.get(host));
            total+=droppedMessagesByHost.get(host);
        }
        write("\nTotal : "+total);
        super.done();
    }

    

}
