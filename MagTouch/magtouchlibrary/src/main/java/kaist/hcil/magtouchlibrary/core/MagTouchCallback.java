package kaist.hcil.magtouchlibrary.core;

import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;

public interface MagTouchCallback {
    void doneStep(CameData cameData);
    void classified(MagTouchRequestPacket packet, CameData cameData, String predictedFinger);
    void respondAfter(MagTouchRequestPacket packet, CameData cameData);
    void stabilized();
    void stateChangedTo(int state);
}
