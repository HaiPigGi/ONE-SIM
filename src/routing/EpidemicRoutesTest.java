package routing;

import core.Settings;

public class EpidemicRoutesTest extends ActiveRouter {

    /**
     * make a constructor
     * 
     * @param set
     */

    public EpidemicRoutesTest(Settings set) {
        super(set);
    }

    /**
     * Copy Constructor
     *
     *
     */
    protected EpidemicRoutesTest(EpidemicRoutesTest set) {
        super(set);
    }
    @Override
    public void update () {
        super.update();

        if (isTransferring() || !canStartTransfer()) {
            return;
        }
        
        if (exchangeDeliverableMessages() != null) {
            return;
        }

        this.tryAllMessagesToAllConnections();
    }
    @Override
    public EpidemicRoutesTest replicate() {
        return new EpidemicRoutesTest(this);
    }

}
