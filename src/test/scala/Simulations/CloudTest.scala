package Simulations

import HelperUtils.{CloudletInit, CreateElements, DataCenterInit, HostInit, VMInit}
import com.typesafe.config.ConfigFactory
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.vms.Vm
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}
import collection.JavaConverters._
import java.util

class CloudTest extends AnyFlatSpec with Matchers {

  val simulationFile = "application"
  val simulationName = "cloudSimulator1"
  val conf = ConfigFactory.load(simulationFile)
  val num_vms = conf.getInt(simulationName+".vm.num_vms")
  val num_cloudlets = conf.getInt(simulationName+".cloudlet.num_cloudlets")
  val num_hosts = conf.getInt(simulationName+".host.num_hosts")


  val simulation = new CloudSim();

  // Test 1
  "Check for cloudsim" should "if it is running" in {
    simulation.isRunning should not be (true)
  }

  val dcTest = new DataCenterInit(simulationFile, simulationName)
  val hostTest = new HostInit(simulationFile, simulationName)
  val vmTest = new VMInit(simulationFile, simulationName)
  val cloudletTest = new CloudletInit(simulationFile, simulationName)

  val cloudElements = new CreateElements(dcTest, hostTest, vmTest, cloudletTest)

  val datacenter = cloudElements.createDatacenter(simulation, new VmAllocationPolicySimple, new VmSchedulerTimeShared, true)

  // Test 2
  "Check for datacenter hosts" should "if they are created and count matches with config file" in {
    datacenter should not be (null)
    assert(datacenter.size() === num_hosts)
  }


  val broker = new DatacenterBrokerSimple(simulation)
  // Test 3
  "Check for broker" should "if it is created" in {
    broker should not be (null)
  }

  val vm: List[Vm] = cloudElements.createVmList(new CloudletSchedulerTimeShared)

  // Test 4
  "Check for VM" should "for its size and match with config file" in {
    assert(vm.size === num_vms)
  }


  val cloudletList: List[Cloudlet] = cloudElements.createCloudletList()

  // Test 5
  "Check for Cloudlet" should "for its size and match with config file" in {
    assert(cloudletList.size === num_cloudlets)
  }

  broker.submitVmList(vm.asJava)

  broker.submitCloudletList(cloudletList.asJava)
}
