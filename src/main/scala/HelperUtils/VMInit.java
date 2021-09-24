package HelperUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class VMInit {
    Config conf, conf1, conf2;
    double  mips;
    int num_pe, ram, bw, size, num_vms;

    public VMInit(String simulationFile, String simulationName) {
        conf        = ConfigFactory.load(simulationFile);
        mips        = conf.getInt(simulationName + ".vm.mips");
        num_pe = conf.getInt(simulationName + ".vm.num_pe");
        ram         = conf.getInt(simulationName + ".vm.ram");
        bw          = conf.getInt(simulationName + ".vm.bw");
        size        = conf.getInt(simulationName + ".vm.size");
        num_vms = conf.getInt(simulationName + ".vm.num_vms");
    }

    public VMInit(String simulationFile1, String simulationFile2, String simulationName) {
        conf1        = ConfigFactory.load(simulationFile1);
        conf2        = ConfigFactory.load(simulationFile2);
        mips        = conf1.getInt(simulationName + ".vm.mips");
        num_pe = conf1.getInt(simulationName + ".vm.num_pe");
        ram         = conf1.getInt(simulationName + ".vm.ram");
        bw          = conf1.getInt(simulationName + ".vm.bw");
        size        = conf1.getInt(simulationName + ".vm.size");
        num_vms = conf2.getInt(simulationName + ".vm.num_vms");
    }
}
