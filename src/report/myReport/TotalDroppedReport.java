package report.myReport;

import java.util.HashMap;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import report.Report;

public class TotalDroppedReport extends Report implements MessageListener {
    private Map<DTNHost, Integer> droppedMessagesByHost;

    // Counters for various message events
    private int nrofDropped;

    /*
     * Constructor
     */
    public TotalDroppedReport() {
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
        // Check if message is a warmup message
        if (isWarmupID(m.getId())) {
            return;
        }
        if (dropped) {
            if (droppedMessagesByHost.containsKey(where)) {
                // jika kunci (host) sudah ada, increment nilai untuk kunci tersebut
                droppedMessagesByHost.put(where, droppedMessagesByHost.get(where) + 1);
            } else {
                // jika kunci (host) belum ada, masukkan kunci tersebut dengan nilai 1
                droppedMessagesByHost.put(where, 1);
            }
        }

    }

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
        int total = 0;
        for (DTNHost host : droppedMessagesByHost.keySet()) {
            write("Node : " + host + ": " + droppedMessagesByHost.get(host));
            total += droppedMessagesByHost.get(host);
        }
        write("\nTotal : " + total);
        super.done();
    }

}
