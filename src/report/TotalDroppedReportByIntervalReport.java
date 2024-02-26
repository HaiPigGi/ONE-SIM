package report;

import java.util.HashMap;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;

public class TotalDroppedReportByIntervalReport extends Report implements MessageListener {
    private Map<DTNHost, Integer> droppedMessagesByHost;
    private Map<DTNHost, Integer> droppedMessagesByHostPerInterval;
    private int interval;
    private int totalDrop;

    /**
     * Make A Constructor
     */

    public TotalDroppedReportByIntervalReport() {
        init();
        this.droppedMessagesByHost = new HashMap<>();
        this.droppedMessagesByHostPerInterval = new HashMap<>();
        this.interval = 5; // Misalnya, Laporan Diberikan Setiap 5 Second
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
            Integer count = droppedMessagesByHost.get(where);

            // Jika belum ada pesan yang dihapus sebelumnya dari host, inisialisasi dengan 0
            if (count == null) {
                count = 0;
            }

            // Menambahkan pesan yang dihapus ke host ke dalam Map dengan menambahkan 1 ke
            // nilai sebelumnya
            droppedMessagesByHost.put(where, count + 1);

            // Memperbarui jumlah pesan yang dihapus dalam interval saat ini untuk host
            // tertentu
            Integer intervalCount = droppedMessagesByHostPerInterval.get(where);

            // Jika belum ada pesan yang dihapus dari host dalam interval ini, inisialisasi
            // dengan 0
            if (intervalCount == null) {
                intervalCount = 0;
            }

            // Menambahkan pesan yang dihapus ke host ke dalam Map interval dengan
            // menambahkan 1 ke nilai sebelumnya
            droppedMessagesByHostPerInterval.put(where, intervalCount + 1);

            // Menambahkan 1 ke total pesan yang dihapus dari semua host
            totalDrop++;
        }

        // Memeriksa apakah sudah waktunya untuk melaporkan jumlah total pesan yang
        // dihapus per host
        if (SimClock.getTime() % interval == 0) {
            // Menghitung waktu awal interval
            double intervalStartTime = SimClock.getTime() - interval;

            // Melaporkan jumlah total pesan yang dihapus per host dalam interval tersebut
            reportTotalDropPerNode(intervalStartTime, getSimTime());

            // Menghapus data pesan yang dihapus dalam interval ini untuk persiapan interval
            // berikutnya
            droppedMessagesByHostPerInterval.clear();
        }
    }

    // methode to report total drop per node per interval
    private void reportTotalDropPerNode(double intervalStartTime, double intervalEndTime) {
        write("Total Dropped Message Per Node Interval " + interval + " Second");
        // Menampilkan waktu mulai interval
        write("Interval Start Time: " + intervalStartTime);
        for (DTNHost host : droppedMessagesByHostPerInterval.keySet()) {
            write("Node || " + host + " : " + droppedMessagesByHostPerInterval.get(host) + "|| Time : "
                    + intervalEndTime);
        }
        write("");
    }

    @Override
    public void done() {
        // get Time Now
        double currentTime = getSimTime();
        // count Time now Until End Interval
        double intervalStartTime = currentTime - interval;
        reportTotalDropPerNode(intervalStartTime, currentTime);
        write("total Drop All Node : " + totalDrop);
        super.done();
    }

}
