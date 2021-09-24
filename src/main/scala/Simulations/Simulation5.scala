package Simulations

import HelperUtils.{CreateElements, CloudletInit, DataCenterInit, HostInit, VMInit}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.slf4j.{Logger, LoggerFactory}

import collection.JavaConverters.*
import java.util

object Simulation5 extends App {
  // Config file with cloud simulator details
  // provider.conf -> This contains simulation details for all the 3 datacenters that would be hidden from the customers
  // consumer.conf -> This contains simulation details for all the 3 datacenters that can be changed depending on customer need
  val simulationFile1 = "provider"
  val simulationFile2 = "consumer"

  // Simulator names for SaaS, PaaS and IaaS
  val simulationNameSaaS = "cloudSimulatorSAAS"
  val simulationNamePaaS = "cloudSimulatorPAAS"
  val simulationNameIaaS = "cloudSimulatorIAAS"

  //Static logger variable to reference the Logger instance
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  logger.info("Starting SaaS, PaaS and IaaS Simulation Execution\n")
  val simulation = new CloudSim();

  // Extracting datacenter specifications separately for SaaS, PaaS, IaaS
  logger.info("Extracting datacenter specifications from config file for SaaS\n")
  val dcSaaSInfo = new DataCenterInit(simulationFile1, simulationNameSaaS)
  val hostSaaSInfo = new HostInit(simulationFile1, simulationNameSaaS)
  val vmSaaSInfo = new VMInit(simulationFile1, simulationNameSaaS)
  val cloudletSaaSInfo = new CloudletInit(simulationFile1, simulationFile2, simulationNameSaaS)

  logger.info("Extracting datacenter specifications from config file for PaaS\n")
  val dcPaaSInfo = new DataCenterInit(simulationFile1, simulationNamePaaS)
  val hostPaaSInfo = new HostInit(simulationFile1, simulationNamePaaS)
  val vmPaaSInfo = new VMInit(simulationFile1, simulationFile2, simulationNamePaaS)
  val cloudletPaaSInfo = new CloudletInit(simulationFile2, simulationNamePaaS)

  logger.info("Extracting datacenter specifications from config file for IaaS\n")
  val dcIaaSInfo = new DataCenterInit(simulationFile1, simulationNameIaaS)
  val hostIaaSInfo = new HostInit(simulationFile1, simulationNameIaaS)
  val vmIaaSInfo = new VMInit(simulationFile2, simulationNameIaaS)
  val cloudletIaaSInfo = new CloudletInit(simulationFile2, simulationNameIaaS)

  // Create object of helper util class for each datacenter operations
  val cloudSaaSElements = new CreateElements(dcSaaSInfo, hostSaaSInfo, vmSaaSInfo, cloudletSaaSInfo)
  val cloudPaaSElements = new CreateElements(dcPaaSInfo, hostPaaSInfo, vmPaaSInfo, cloudletPaaSInfo)
  val cloudIaaSElements = new CreateElements(dcIaaSInfo, hostIaaSInfo, vmIaaSInfo, cloudletIaaSInfo)

  // Create a new datacenter for simulation with Simple VM Allocation Policy and Space Shared VM Scheduler
  val dcSAAS: Datacenter = cloudSaaSElements.createDatacenter(simulation, new VmAllocationPolicySimple,
    new VmSchedulerSpaceShared, true)
  val dcPAAS: Datacenter = cloudPaaSElements.createDatacenter(simulation, new VmAllocationPolicySimple,
    new VmSchedulerSpaceShared, true)
  val dcIAAS: Datacenter = cloudIaaSElements.createDatacenter(simulation, new VmAllocationPolicySimple,
    new VmSchedulerSpaceShared, true)

  // Create a broker which decides which Cloudlet executes first and in which VM it runs
  val broker = new DatacenterBrokerSimple(simulation)

  // Instantiates a Network Topology from a file inside the application's resource director
  val networkTopology = BriteNetworkTopology.getInstance("topology.brite")
  simulation.setNetworkTopology(networkTopology)

  // Adding SaaS, PaaS, IaaS datacenters and broker as nodes of BRITE topology
  networkTopology.mapNode(dcSAAS, 0)
  networkTopology.mapNode(dcPAAS, 2)
  networkTopology.mapNode(dcIAAS, 3)
  networkTopology.mapNode(broker, 4)


  // Create list of vm for each cloud model with Time Shared Cloudlet Scheduler
  logger.info("Creating VM")
  val vmList: List[Vm] = cloudSaaSElements.createVmList(new CloudletSchedulerTimeShared) :::
    cloudPaaSElements.createVmList(new CloudletSchedulerTimeShared) :::
    cloudIaaSElements.createVmList(new CloudletSchedulerTimeShared)

  // Create list of cloudlets for each cloud model
  logger.info("Creating Cloudlets")
  val cloudletList: List[Cloudlet] = cloudSaaSElements.createCloudletList() :::
    cloudPaaSElements.createCloudletList() :::
    cloudIaaSElements.createCloudletList()

  // Submit vm list to broker
  logger.info("VMs created & submitting to the broker")
  broker.submitVmList(vmList.asJava)

  //Submit cloudlet list to broker
  logger.info("VMs created & submitting to the broker")
  broker.submitCloudletList(cloudletList.asJava)

  // Execute simulation
  simulation.start

 // Get list of finished Cloudlets with their statistics
 val finishedCloudlets: List[Cloudlet] = broker.getCloudletFinishedList.asScala.toList
  new CloudletsTableBuilder(finishedCloudlets.asJava).build()

  // Calculate cost of executing the simulation for every cloudlet
  val mapList = finishedCloudlets.map(cloudSaaSElements.calculateCost)

  // Reduces the list of maps based on the datacenter values
  // List(Map(DataCenter 1, 10), Map(DataCenter 1, 15), Map(DataCenter 2, 5)) -> List(Map(DataCenter 1, 25), Map(DataCenter 2, 5))
  val mm = cloudSaaSElements.mergeMap(mapList)((v1, v2) => v1 + v2)
  mm.foreach{keyVal => logger.info("{} cost = {}",keyVal._1, keyVal._2)}
  System.out.println()

  logger.info("Ending SaaS, PaaS and IaaS Simulation Execution\n")
}
