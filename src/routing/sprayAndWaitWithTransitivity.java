package routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Connection;
import core.Message;
import core.Settings;
import core.SimClock;

public class sprayAndWaitWithTransitivity extends ActiveRouter {

    /** Inisiasi Variable Final */
    public static final String numberOfCopies = "numberofcopies";
    public static final String binaryMode = "binaryMode";
    public static final String SprayAndWait_nm = "sprayAndwaitRoutertrainsitivity"; // spray And wait Name Space
    public static final String msg_count_property = SprayAndWait_nm + "." + "copies";
    /** delivery predictability initialization constant */
    public static final double P_INIT = 0.75;

    /**
     * Predictability aging constant (gamma) -setting id ({@value}).
     * Default value for setting is {@link #DEFAULT_GAMMA}.
     */
    public static final String GAMMA_S = "gamma";

    /** Stores information about nodes with which this host has come in contact */
    protected Map<DTNHost, EncounterInfo> recentEncounters;
    protected Map<DTNHost, Map<DTNHost, EncounterInfo>> neighborEncounters;

    public int initalNumberOfCopies;
    public boolean isBinary;
    private DTNHost forwardTo;
    private double someThreshold;

    /** delivery predictabilities */
    private Map<DTNHost, Double> preds;
    /** last delivery predictability update (sim)time */
    private double lastAgeUpdate;
    /** the value of nrof seconds in time unit -setting */
    private int secondsInTimeUnit;

    /** value of gamma setting */
    private double gamma;

    /** value of beta setting */
    private double beta;

    /** Make the Constructor */
    public sprayAndWaitWithTransitivity(Settings setting) {
        super(setting);

        // make an object for setting
        Settings SprayWaitSet = new Settings(SprayAndWait_nm);

        // make the initialNumberOfCopies
        initalNumberOfCopies = SprayWaitSet.getInt(numberOfCopies);
        isBinary = SprayWaitSet.getBoolean(binaryMode);

        recentEncounters = new HashMap<DTNHost, EncounterInfo>();
        neighborEncounters = new HashMap<DTNHost, Map<DTNHost, EncounterInfo>>();
        this.someThreshold = 10.0;
        forwardTo = null;

        initPreds();
    }

    /**
     * make the Copy Constructor
     * 
     * @param snw
     */
    protected sprayAndWaitWithTransitivity(sprayAndWaitWithTransitivity snw) {
        super(snw);

        initalNumberOfCopies = snw.initalNumberOfCopies;
        isBinary = snw.isBinary;

        this.secondsInTimeUnit = snw.secondsInTimeUnit;
		this.beta = snw.beta;
		this.gamma = snw.gamma;

        recentEncounters = new HashMap<DTNHost, EncounterInfo>();
        neighborEncounters = new HashMap<DTNHost, Map<DTNHost, EncounterInfo>>();
        initPreds();
    }

    /* Make The Receving Message */
    @Override
    public int receiveMessage(Message msg, DTNHost from) {
        return super.receiveMessage(msg, from);
    }

    /**
	 * Initializes predictability hash
	 */
	private void initPreds() {
		this.preds = new HashMap<DTNHost, Double>();
	}

    /**
     * Called whenever a connection goes up or comes down.
     */
    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);

        // Update recent encounters information
        if (con.isUp()) {
            DTNHost otherHost = con.getOtherNode(getHost());
            updateDeliveryPredFor(otherHost);
            updateTransitivePreds(otherHost);
        }
    }

    /**
     * Updates delivery predictions for a host.
     * <CODE>P(a,b) = P(a,b)_old + (1 - P(a,b)_old) * P_INIT</CODE>
     * 
     * @param host The host we just met
     */
    private void updateDeliveryPredFor(DTNHost host) {
        double oldValue = getPredFor(host);
        double newValue = oldValue + (1 - oldValue) * P_INIT;
        preds.put(host, newValue);
    }

    /**
     * Returns the current prediction (P) value for a host or 0 if entry for
     * the host doesn't exist.
     * 
     * @param host The host to look the P for
     * @return the current P value
     */
    public double getPredFor(DTNHost host) {
        ageDeliveryPred(); // make sure preds are updated before getting
        if (preds.containsKey(host)) {
            return preds.get(host);
        } else {
            return 0;
        }
    }

    /**
     * Ages all entries in the delivery predictions.
     * <CODE>P(a,b) = P(a,b)_old * (GAMMA ^ k)</CODE>, where k is number of
     * time units that have elapsed since the last time the metric was aged.
     * 
     * @see #SECONDS_IN_UNIT_S
     */

    private void ageDeliveryPred() {
        double timeDiff = (SimClock.getTime() - this.lastAgeUpdate) /
                secondsInTimeUnit;

        if (timeDiff == 0) {
            return;
        }

        double mult = Math.pow(gamma, timeDiff);
        for (Map.Entry<DTNHost, Double> e : preds.entrySet()) {
            e.setValue(e.getValue() * mult);
        }
        this.lastAgeUpdate = SimClock.getTime();
    }

    /**
     * Returns a map of this router's delivery predictions
     * 
     * @return a map of this router's delivery predictions
     */
    private Map<DTNHost, Double> getDeliveryPreds() {
        ageDeliveryPred(); // make sure the aging is done
        return this.preds;
    }

    /**
     * Updates transitive (A->B->C) delivery predictions.
     * <CODE>P(a,c) = P(a,c)_old + (1 - P(a,c)_old) * P(a,b) * P(b,c) * BETA
     * </CODE>
     * 
     * @param host The B host who we just met
     */
    private void updateTransitivePreds(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof sprayAndWaitWithTransitivity : "Spray And Wait only works " +
                " with other routers of same type";


        double pForHost = getPredFor(host); // P(a,b)
        Map<DTNHost, Double> othersPreds = ((sprayAndWaitWithTransitivity) otherRouter).getDeliveryPreds();
        System.out.println("cek Map : "+ othersPreds);
        for (Map.Entry<DTNHost, Double> e : othersPreds.entrySet()) {
            if (e.getKey() == getHost()) {
                continue; // don't add yourself
            }

            double pOld = getPredFor(e.getKey()); // P(a,c)_old
            double pNew = pOld + (1 - pOld) * pForHost * e.getValue() * beta;
            preds.put(e.getKey(), pNew);
        }
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message msg = super.messageTransferred(id, from);

        // Periksa apakah pesan yang ditransfer memiliki informasi pertemuan antar node
        Map<DTNHost, EncounterInfo> peerEncounters = neighborEncounters.get(from);
        // System.out.println("Peer Encounters: " + peerEncounters);

        if (peerEncounters != null) {
            for (Map.Entry<DTNHost, EncounterInfo> entry : peerEncounters.entrySet()) {
                DTNHost neighbor = entry.getKey();
                System.out.println(neighbor);
                // Lewati pertemuan dengan diri sendiri atau tetangga yang sudah terhubung
                if (neighbor == getHost() || getConnections().contains(neighbor)) {
                    continue;
                }

                // Periksa apakah tetangga lebih dekat ke tujuan daripada target saat ini
                DTNHost target = msg.getTo(); // Asumsikan metode getter untuk tujuan
                double distanceToTarget = getHost().getLocation().distance(target.getLocation());
                double distanceToNeighbor = getHost().getLocation().distance(neighbor.getLocation());

                // Jika tetangga lebih dekat, pertimbangkan untuk meneruskan pesan melalui
                // mereka
                if (distanceToNeighbor < distanceToTarget) {
                    EncounterInfo encounterInfo = entry.getValue();
                    double encounterAge = SimClock.getTime() - encounterInfo.getLastSeenTime();
                    if (encounterAge < someThreshold) {
                        // Perbarui tujuan pesan dengan tetangga untuk transitivity
                        forwardTo = neighbor;
                        break; // Hanya meneruskan ke satu tetangga pada satu waktu
                    }
                }
            }
        }

        // Periksa apakah pesan sudah dikirim dan apakah pesan memiliki informasi
        // pertemuan
        if (isDeliveredMessage(msg) && peerEncounters != null) {
            Integer numberOfCopies = (Integer) msg.getProperty(msg_count_property);

            assert numberOfCopies != null : "Ini bukan pesan Spray And Wait: " + msg;

            // Periksa apakah kopi akan dikirim secara biner atau tidak
            if (isBinary) {
                numberOfCopies = (int) Math.floor(numberOfCopies / 2.0);
            } else {
                // Kode standar dengan satu salinan pesan
                numberOfCopies = 1;
            }
            msg.updateProperty(msg_count_property, numberOfCopies);
        }

        return msg;
    }

    /**
     * Membuat pesan baru dan mengirimkannya ke tujuan yang sesuai berdasarkan
     * transitivity.
     * 
     * @param msg Pesan yang akan dibuat dan dikirim
     * @return true jika pesan berhasil dibuat dan dikirim, false jika tidak
     */
    @Override
    public boolean createNewMessage(Message msg) {
        makeRoomForMessage(msg.getSize());

        msg.setTtl(this.msgTtl);
        msg.addProperty(msg_count_property, new Integer(initalNumberOfCopies));
        addToMessages(msg, true);
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (!canStartTransfer() || isTransferring()) {
            return;
        }

        if (exchangeDeliverableMessages() != null) {
            return;
        }

        // Mengirim pesan yang masih memiliki salinan tersisa
        List<Message> copiesLeft = getMessagesWithCopiesLeft();
        if (!copiesLeft.isEmpty()) {
            // Cek semua koneksi yang tersedia untuk host saat ini
            for (Connection connection : getConnections()) {
                // Perbarui informasi pertemuan untuk setiap tetangga yang terhubung
                updateEncounterInfo(connection.getOtherNode(getHost()));
            }

            // Mencoba mengirim pesan ke semua koneksi yang tersedia
            tryMessagesToConnections(copiesLeft, getConnections());

            // Jika terdapat pesan yang akan diteruskan, kirim pesan ke tetangga yang
            // dipilih
            if (forwardTo != null) {
                tryMessagesToConnections(copiesLeft, getConnectionsToForward());
                forwardTo = null; // Reset forwardTo setelah meneruskan pesan
            }
        }
    }

    /**
     * Returns the list of connections to forward messages to the selected neighbor.
     */
    private List<Connection> getConnectionsToForward() {
        List<Connection> selectedConnections = new ArrayList<>();
        if (forwardTo != null) {
            // Cari koneksi yang terhubung dengan tetangga yang dipilih
            for (Connection connection : getConnections()) {
                if (connection.getOtherNode(getHost()).equals(forwardTo)) {
                    selectedConnections.add(connection);
                }
            }
            forwardTo = null; // Reset forwardTo setelah meneruskan pesan
        }
        System.out.println(forwardTo);
        return selectedConnections;
    }

    /**
     * Updates encounter information for the given peer.
     * 
     * @param peer The peer for which encounter information needs to be updated
     */
    private void updateEncounterInfo(DTNHost peer) {
        // Check if encounter info already exists for the peer
        if (recentEncounters.containsKey(peer)) {
            // Encounter info exists, update the encounter time
            EncounterInfo info = recentEncounters.get(peer);
            info.updateEncounterTime(SimClock.getTime());
        } else {
            // Encounter info doesn't exist, create new encounter info
            recentEncounters.put(peer, new EncounterInfo(SimClock.getTime()));
        }
    }

    /**
     * Creates and returns a list of messages this router is currently
     * carrying and still has copies left to distribute (nrof copies > 1).
     * 
     */
    protected List<Message> getMessagesWithCopiesLeft() {
        List<Message> list = new ArrayList<Message>();

        // create loop for Message
        for (Message msg : getMessageCollection()) {
            Integer numberOfCopies = (Integer) msg.getProperty(msg_count_property);
            assert numberOfCopies != null : "spray And Wait " + msg +
                    "didn't have " + "Number of copies";
            if (numberOfCopies > 1) {
                list.add(msg);
            }
        }
        return list;

    }

    /**
     * Called just before a transfer is finalized (by
     * {@link ActiveRouter#update()}).
     * Reduces the number of copies we have left for a message.
     * In binary Spray and Wait, sending host is left with floor(n/2) copies,
     * but in standard mode, nrof copies left is reduced by one.
     */
    @Override
    protected void transferDone(Connection con) {
        Message msg = getMessage(con.getMessage().getId());

        if (msg == null) {
            return;
        }

        Integer numberOfCopies = (Integer) msg.getProperty(msg_count_property);

        // mengurangi jumlah salinan pesan sesuai dengan mode yang dipilih
        if (isBinary) {
            numberOfCopies = (int) Math.ceil(numberOfCopies / 2.0);
        } else {
            numberOfCopies--;
        }
        msg.updateProperty(msg_count_property, numberOfCopies);

    }

    @Override
    public sprayAndWaitWithTransitivity replicate() {
        return new sprayAndWaitWithTransitivity(this);
    }

    /**
     * Stores all necessary info about encounters made by this host to some other
     * host.
     * At the moment, all that's needed is the timestamp of the last time these two
     * hosts
     * met.
     * 
     */
    protected class EncounterInfo {
        protected double seenAtTime;

        public EncounterInfo(double atTime) {
            this.seenAtTime = atTime;
        }

        public void updateEncounterTime(double atTime) {
            this.seenAtTime = atTime;
        }

        public double getLastSeenTime() {
            return seenAtTime;
        }

        public void updateLastSeenTime(double atTime) {
            this.seenAtTime = atTime;
        }
    }

}
