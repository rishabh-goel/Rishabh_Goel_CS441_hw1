package HelperUtils

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.slf4j.{Logger, LoggerFactory}

import collection.JavaConverters.*
import scala.collection.mutable

class CreateElements(val datacenter: DataCenterInit, val host: HostInit, val vm: VMInit, cloudlet: CloudletInit) {
  var logger: Logger = LoggerFactory.getLogger(classOf[CreateElements].getSimpleName)


  //Create DataCenter
  def createDatacenter(simulation: CloudSim, vmAllocPolicy: VmAllocationPolicy, vmSchedulerPolicy: VmScheduler, activate: Boolean): Datacenter = { //List of Datacenter's Hosts
    val hostList: List[Host] = createHostList(activate, vmSchedulerPolicy)
    val dc: Datacenter = new DatacenterSimple(simulation, hostList.asJava, vmAllocPolicy)
    // set characteristics of the datacenter
    dc.getCharacteristics.setCostPerSecond(datacenter.costPerSecond).setCostPerMem(datacenter.costPerMemory).setCostPerStorage(datacenter.costPerStorage).setCostPerBw(datacenter.costPerBw)
    return dc
  }

  //Create list of Hosts
  def createHostList(activateHosts: Boolean, scheduler: VmScheduler): List[Host] = {

    logger.info("Adding {} hosts to the datacenter", host.num_hosts)
    // Java Stream to create multiple hosts
    val list: List[Host] = List.tabulate(host.num_hosts)(i => {
        logger.info("Creating host {} and adding PEs ", i)
        createHost(activateHosts, scheduler)
    })
    list
  }

  // Create a single Host
  @throws[Exception]
  def createHost(activateHost: Boolean, scheduler: VmScheduler): Host = { //List of Host's PEs
    // Java Stream used to add PEs in the list
    val list: List[Pe] = List.tabulate(host.num_pe)(i => {
        new PeSimple(host.mips)
    })
    // Create a new instance of VmScheduler for each hostr
    val sched: VmScheduler = scheduler.getClass.newInstance
    val host0: Host = new HostSimple(host.ram, host.bw, host.storage, list.asJava, activateHost).setVmScheduler(sched)
    host0
  }

  // Create a list of VMs
  def createVmList(cloudletScheduler: CloudletScheduler): List[Vm] = {
    logger.info("Provisioning {} Vms for allocation to datacenter hosts\n", vm.num_vms)
    // Java Stream used to add create VMs in the list and set their characteristics
    val list: List[Vm] = List.tabulate(vm.num_vms)(i => new VmSimple(vm.mips, vm.num_pe, cloudletScheduler).setRam(vm.ram).setBw(vm.bw).setSize(vm.size))
    return list
  }

  // Create a list of Cloudlets
  def createCloudletList() : List[Cloudlet] = {

    logger.info("Received {} cloudlets for execution in the datacenter\n", cloudlet.num_cloudlets)
    // Java Stream used to add create Cloudlets in the list and set their characteristics
    val list: List[Cloudlet] = List.tabulate(cloudlet.num_cloudlets)(i => new CloudletSimple(cloudlet.length, cloudlet.num_pe).setSizes(cloudlet.size))
    return list
  }

  // Calculate simulation cost
  def calculateCost(cloudlet : Cloudlet): Map[Datacenter, Float] ={

    var dcCosts: Map[Datacenter, Float] = Map[Datacenter, Float]()
    dcCosts += (cloudlet.getVm.getHost.getDatacenter, cloudlet.getTotalCost.asInstanceOf[Float])
    dcCosts

  }

  // Adds the execution time of cloudlets in each datacenter
  def mergeMap[A, B](ms: List[Map[A, B]])(f: (B, B) => B): Map[A, B] =
    (Map[A, B]() /: (for (m <- ms; kv <- m) yield kv)) { (a, kv) =>
      a + (if (a.contains(kv._1)) kv._1 -> f(a(kv._1), kv._2) else kv)
    }
}
