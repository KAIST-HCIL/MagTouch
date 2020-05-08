package kaist.hcil.magtouchlibrary.core;

import kaist.hcil.magtouchlibrary.datamodel.CameData;

public abstract class CameCallback {
    abstract void doneStep(CameData cameData);
    abstract void stateChangedTo(int state);
}
