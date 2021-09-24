package Simulations

import HelperUtils.{CreateElements, CloudletInit, DataCenterInit, HostInit, VMInit}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.slf4j.{Logger, LoggerFactory}

import collection.JavaConverters.*
import scala.collection.JavaConverters.*
import java.util
import scala.concurrent.JavaConversions

object Simulation1 extends App {

  //Config file with cloud simulator details
  val simulationFile = "application"
  val simulationName = "cloudSimulator1"

  //Static logger variable to reference the Logger instance
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  logger.info("Starting VM & Cloudlet TimeShared Simulation Execution\n")
  val simulation = new CloudSim();

  logger.info("Extracting datacenter specifications for Simulation 1\n")
  val dcInfo = new DataCenterInit(simulationFile, simulationName)
  val hostInfo = new HostInit(simulationFile, simulationName)
  val vmInfo = new VMInit(simulationFile, simulationName)
  val cloudletInfo = new CloudletInit(simulationFile, simulationName)

  // Create instance of helper util class for datacenter operations
  val cloudElements = new CreateElements(dcInfo, hostInfo, vmInfo, cloudletInfo)

  // Create a datacenter for simulation with Simple VM Allocation Policy and Time Shared VM Scheduler
  logger.info("Creating Datacenter\n")
  cloudElements.createDatacenter(simulation, new VmAllocationPolicySimple, new VmSchedulerTimeShared, true)

  // Create a broker which decides which Cloudlet executes first and in which VM it runs
  logger.info("Creating Broker\n")
  val broker = new DatacenterBrokerSimple(simulation)
  logger.info("Broker = {}\n", broker.getClass.getSimpleName)

  // Create list of vm with Time Shared Cloudlet Scheduler
  logger.info("Creating VM")
  val vm: List[Vm] = cloudElements.createVmList(new CloudletSchedulerTimeShared)

  // Create list of cloudlets
  logger.info("Creating Cloudlets")
  val cloudletList: List[Cloudlet] = cloudElements.createCloudletList()


  // Submit vm list to broker
  logger.info("VMs created & submitting to the broker")
  broker.submitVmList(vm.asJava)

  // Submit cloudlet list to broker
  logger.info("Cloudlets created & submitting to the broker")
  broker.submitCloudletList(cloudletList.asJava)

  // Execute simulation
  simulation.start

  // Get list of finished Cloudlets with their statistics
  val finishedCloudlets: List[Cloudlet] = broker.getCloudletFinishedList.asScala.toList
  new CloudletsTableBuilder(finishedCloudlets.asJava).build()

  // Calculate cost of executing the simulation for every cloudlet
  val mapList = finishedCloudlets.map(cloudElements.calculateCost)

  // Reduces the list of maps based on the datacenter values
  // List(Map(DataCenter 1, 10), Map(DataCenter 1, 15), Map(DataCenter 2, 5)) -> List(Map(DataCenter 1, 25), Map(DataCenter 2, 5))
  val mm = cloudElements.mergeMap(mapList)((v1, v2) => v1 + v2)
  mm.foreach{keyVal => logger.info("{} cost = {}",keyVal._1, keyVal._2)}

  logger.info("Ending VM & Cloudlet TimeShared Simulation Execution\n")
}
