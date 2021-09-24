package HelperUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * A util class to parse host specifications from config file
 */

public class HostInit {

    Config conf;
    int  ram, storage, bw, num_pe, mips, num_hosts;

    public HostInit(String simulationFile, String simulationName) {
        conf        = ConfigFactory.load(simulationFile);
        ram         = conf.getInt(simulationName + ".host.ram");
        storage     = conf.getInt(simulationName + ".host.storage");
        bw          = conf.getInt(simulationName + ".host.bw");
        num_pe = conf.getInt(simulationName + ".host.num_pe");
        mips        = conf.getInt(simulationName + ".host.mips");
        num_hosts   = conf.getInt(simulationName + ".host.num_hosts");
    }
}
