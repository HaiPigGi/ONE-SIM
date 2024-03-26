package report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import core.DTNHost;
import core.Message;
import core.SimClock;
import core.UpdateListener;

public class bufferOcReport extends Report implements UpdateListener {
    private Map<DTNHost, List<Double>> bufferOccupancy;
    private int interval;
    private double lastRecord;

    public bufferOcReport() {
        init();
        this.interval = 5; // Set interval to 5 seconds
        this.lastRecord = -1; // Initialize lastRecord to an invalid value
        this.bufferOccupancy = new HashMap<>(); // Initialize bufferOccupancy map
    }

    protected void init() {
        super.init();
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        // Handle message deletion if necessary
    }

    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            recordBufferOccupancy(hosts); // Record buffer occupancy for each host
        }
    }

    private void recordBufferOccupancy(List<DTNHost> hosts) {
        for (DTNHost h : hosts) {
            double tmp = h.getBufferOccupancy();
            tmp = (tmp <= 100.0) ? (tmp) : (100.0);
            List<Double> occupancyList = bufferOccupancy.getOrDefault(h, new ArrayList<>());
            occupancyList.add(tmp);
            bufferOccupancy.put(h, occupancyList); // Store buffer occupancy for each host
        }
    }

    @Override
    public void done() {
        String stats = "";
        for (Map.Entry entry : this.bufferOccupancy.entrySet()) {
            stats +=entry.getKey()+" : ";
            stats +=entry.getValue()+" \n";
        }

        write(stats);
        super.done();
    }
}
