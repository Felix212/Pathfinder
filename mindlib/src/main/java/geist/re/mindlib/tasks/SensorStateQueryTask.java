package geist.re.mindlib.tasks;

import android.util.Log;

import geist.re.mindlib.RobotService;
import geist.re.mindlib.hardware.Sensor;

/**
 * Created by sbk on 14.03.17.
 */

public class SensorStateQueryTask extends RobotQueryTask {
    public static final int IDX_SENSOR_PORT = 4;
    byte [] query = {0x03,0x00,(byte)0x00,0x07,0x00};

    public SensorStateQueryTask(Sensor s){
        query[IDX_SENSOR_PORT] = s.getRawPort();
    }

    @Override
    public byte[] getRawQuery() {
        return query;
    }

    @Override
    public void execute(RobotService rs) {
        Log.d(TAG, "Executing robot sensor state query");
        rs.writeToNXTSocket(getRawQuery());
    }
}
