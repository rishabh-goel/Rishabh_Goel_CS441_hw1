package HelperUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DataCenterInit {
    Config conf;
    double costPerSecond, costPerMemory, costPerStorage, costPerBw;

    public DataCenterInit(String simulationFile, String simulationName) {
        conf                = ConfigFactory.load(simulationFile);
        costPerSecond       = conf.getDouble(simulationName + ".dataCenter.costPerSecond");
        costPerMemory       = conf.getDouble(simulationName + ".dataCenter.costPerMemory");
        costPerStorage      = conf.getDouble(simulationName + ".dataCenter.costPerStorage");
        costPerBw           = conf.getDouble(simulationName + ".dataCenter.costPerStorage");
    }
}
