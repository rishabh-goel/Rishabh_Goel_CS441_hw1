package HelperUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class CloudletInit {
    Config conf, conf1, conf2;
    int  length, num_pe, size, num_cloudlets;

    public CloudletInit(String simulationFile1, String simulationFile2, String simulationName) {
        conf1        = ConfigFactory.load(simulationFile1);
        conf2        = ConfigFactory.load(simulationFile2);
        length      = conf1.getInt(simulationName + ".cloudlet.length");
        num_pe      = conf1.getInt(simulationName + ".cloudlet.num_pe");
        size        = conf1.getInt(simulationName + ".cloudlet.size");
        num_cloudlets = conf2.getInt(simulationName + ".cloudlet.num_cloudlets");
    }

    public CloudletInit(String simulationFile, String simulationName) {
        conf        = ConfigFactory.load(simulationFile);
        length      = conf.getInt(simulationName + ".cloudlet.length");
        num_pe = conf.getInt(simulationName + ".cloudlet.num_pe");
        size        = conf.getInt(simulationName + ".cloudlet.size");
        num_cloudlets = conf.getInt(simulationName + ".cloudlet.num_cloudlets");
    }
}
