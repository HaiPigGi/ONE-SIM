package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;

public class droppPerIntervalReport extends Report implements MessageListener {
    private Map<DTNHost, Integer> droppedMessagesByHost;
    // private Map<DTNHost, Integer> droppedMessagesByHostPerInterval;
    private int interval;
    private int totalDrop;
    private double lastRecord = Double.MIN_VALUE;
    // Inisialisasi map droppedMessagesByHostPerInterval
    Map<DTNHost, List<Integer>> droppedMessagesByHostPerInterval = new HashMap<>();

    /**
     * Make A Constructor
     */

    public droppPerIntervalReport() {
        init();
        this.droppedMessagesByHost = new HashMap<>();
        this.droppedMessagesByHostPerInterval = new HashMap<>();
        this.interval = 300; // Misalnya, Laporan Diberikan Setiap 5 Second
        this.totalDrop = 0;
    }

    protected void init() {
        super.init();
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

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        // Memeriksa apakah ID pesan terkait dengan masa pemanasan simulasi
        if (isWarmupID(m.getId())) {
            // Jika iya, keluar dari metode karena pesan tidak relevan untuk laporan
            return;
        }

        // Memperbarui jumlah total pesan yang dihapus untuk host tertentu
        if (dropped) {
            // Mendapatkan jumlah pesan yang dihapus sebelumnya dari host
            List<Integer> counts = droppedMessagesByHostPerInterval.getOrDefault(where, new ArrayList<>());

            // Menambahkan pesan yang dihapus ke dalam list counts
            counts.add(counts.size(), 1);
            droppedMessagesByHostPerInterval.put(where, counts);

            // Menambahkan 1 ke total pesan yang dihapus dari semua host
            totalDrop++;
        }

        // Memeriksa apakah sudah waktunya untuk melaporkan jumlah total pesan yang
        // dihapus per host
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            
        }
    }


    @Override
    public void done() {
        // printDroppedPerInterval();
        String stats= "";
        for (Map.Entry entry : this.droppedMessagesByHostPerInterval.entrySet()) {
            stats +=entry.getKey()+"";
            stats +=entry.getValue()+"";
        }
        super.done();
    }

}
