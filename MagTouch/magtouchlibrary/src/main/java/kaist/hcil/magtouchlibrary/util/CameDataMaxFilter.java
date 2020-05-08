package kaist.hcil.magtouchlibrary.util;

import kaist.hcil.magtouchlibrary.datamodel.CameData;

public class CameDataMaxFilter<T extends CameData> extends MaxFilter<T>  {
    @Override
    public double calVal(CameData data) {
        return data.fingerMag.getNorm();
    }
}
