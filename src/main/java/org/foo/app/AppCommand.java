
package org.foo.app;

import org.onosproject.net.PortNumber;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onlab.packet.Ip6Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onlab.packet.VlanId;
import org.onosproject.cli.net.EthType;
import org.onosproject.cli.net.IpProtocol;
import org.onosproject.cli.net.Icmp6Code;
import org.onosproject.cli.net.Icmp6Type;
import org.onosproject.cli.net.ExtHeader;
import org.onosproject.net.EncapsulationType;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.PointToPointIntent;
import static org.onosproject.net.intent.constraint.ProtectionConstraint.protection;

import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.EncapsulationConstraint;
import org.onosproject.net.intent.constraint.PartialFailureConstraint;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.utils.Comparators;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowEntry.FlowEntryState;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.config.basics.BasicLinkConfig;
import org.onosproject.net.config.basics.BandwidthCapacity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
//import java.lang.String;

import static com.google.common.collect.Lists.newArrayList;

import org.onosproject.cli.AbstractShellCommand;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.flow.TypedStoredFlowEntry;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.statistic.FlowStatisticService;
import org.onosproject.net.statistic.SummaryFlowEntryWithLoad;
import org.onosproject.net.statistic.TypedFlowEntryWithLoad;
import org.onosproject.net.PortNumber;
import org.onosproject.net.ConnectPoint;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import java.util.LinkedList;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.onosproject.ui.topo.BaseLink;
import org.onosproject.ui.topo.BaseLinkMap;

import org.onosproject.cli.net.DevicesListCommand;
import org.onosproject.cli.net.DevicePortStatsCommand;

import com.google.common.collect.ImmutableSet;
import org.onosproject.cluster.NodeId;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.device.PortStatistics;
import org.onosproject.net.Element;
import org.onosproject.net.Host;
import org.onosproject.net.Link;
import org.onosproject.net.Port;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.BasicDeviceConfig;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.table.TableModel;
import org.onosproject.ui.table.TableRequestHandler;
import org.onosproject.ui.table.cell.EnumFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;

import org.onlab.util.Bandwidth;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.config.Config;
import com.google.common.base.MoreObjects;

import org.onosproject.net.behaviour.ConfigGetter;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang.WordUtils.capitalizeFully;
import static org.onosproject.net.DeviceId.deviceId;


/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "sample",
         description = "Sample Apache Karaf CLI command")
public class AppCommand extends  AbstractShellCommand {
  

    private static final String BANDWIDTH = "bandwidth";
    private static final Predicate<FlowEntry> TRUE_PREDICATE = f -> true;

    public static final String ANY = "any";

    private static final String LONG_FORMAT = "    id=%s, bytes=%s, "
            + "packets=%s";

    private static final String SHORT_FORMAT = "  bytes=%s, packets=%s, ";

 	
    public static final String CONFIG_KEY = "bandwidthCapacity";

    // JSON key
    private static final String CAPACITY = "capacityMbps";
          

    @Argument(index = 0, name = "state", description = "Flow Rule state",
            required = false, multiValued = false)
    String state = null;

    @Argument(index = 1, name = "uri", description = "Device ID",
              required = false, multiValued = false)
    String uri = null;

    @Argument(index = 2, name = "table", description = "Table ID",
            required = false, multiValued = false)
    String table = null;

    @Option(name = "-s", aliases = "--short",
            description = "Print more succinct output for each flow",
            required = false, multiValued = false)
    private boolean shortOutput = false;

    @Option(name = "-n", aliases = "--no-core-flows",
            description = "Suppress core flows from output",
            required = false, multiValued = false)
    private boolean suppressCoreOutput = false;

    @Option(name = "-c", aliases = "--count",
            description = "Print flow count only",
            required = false, multiValued = false)

    @Argument(index = 3, name = "portNumber", description = "Port Number",
            required = false, multiValued = false)
    Integer portNumber = null;

    private static final String FORMAT =
            "   port=%s, pktRx=%s, pktTx=%s, bytesRx=%s, bytesTx=%s, pktRxDrp=%s, pktTxDrp=%s, Dur=%s";

    private boolean countOnly = false;

    private Predicate<FlowEntry> predicate = TRUE_PREDICATE;

    private static final String FMT = "src=%s/%s, dst=%s/%s, type=%s, state=%s%s, expected=%s";
    private static final String COMPACT = "%s/%s-%s/%s";



	   @Argument(index = 4, name = "ingressDevice",
              description = "Ingress Device/Port Description",
              required = false, multiValued = false)
    String ingressDeviceString = null;

    @Argument(index = 5, name = "egressDevice",
              description = "Egress Device/Port Description",
              required = false, multiValued = false)
    String egressDeviceString = null;

    // -p already defined in ConnectivityIntentCommand
    @Option(name = "-r", aliases = "--protect",
            description = "Utilize path protection",
            required = false, multiValued = false)
    private boolean backup = false;



       // Selectors
    @Option(name = "-s", aliases = "--ethSrc", description = "Source MAC Address",
            required = false, multiValued = false)
    private String srcMacString = null;

    @Option(name = "-d", aliases = "--ethDst", description = "Destination MAC Address",
            required = false, multiValued = false)
    private String dstMacString = null;

    @Option(name = "-t", aliases = "--ethType", description = "Ethernet Type",
            required = false, multiValued = false)
    private String ethTypeString = null;

    @Option(name = "-v", aliases = "--vlan", description = "VLAN ID",
            required = false, multiValued = false)
    private String vlanString = null;

    @Option(name = "--ipProto", description = "IP Protocol",
            required = false, multiValued = false)
    private String ipProtoString = null;

    @Option(name = "--ipSrc", description = "Source IP Prefix",
            required = false, multiValued = false)
    private String srcIpString = null;

    @Option(name = "--ipDst", description = "Destination IP Prefix",
            required = false, multiValued = false)
    private String dstIpString = null;

    @Option(name = "--fLabel", description = "IPv6 Flow Label",
            required = false, multiValued = false)
    private String fLabelString = null;

    @Option(name = "--icmp6Type", description = "ICMPv6 Type",
            required = false, multiValued = false)
    private String icmp6TypeString = null;

    @Option(name = "--icmp6Code", description = "ICMPv6 Code",
            required = false, multiValued = false)
    private String icmp6CodeString = null;

    @Option(name = "--ndTarget", description = "IPv6 Neighbor Discovery Target Address",
            required = false, multiValued = false)
    private String ndTargetString = null;

    @Option(name = "--ndSLL", description = "IPv6 Neighbor Discovery Source Link-Layer",
            required = false, multiValued = false)
    private String ndSllString = null;

    @Option(name = "--ndTLL", description = "IPv6 Neighbor Discovery Target Link-Layer",
            required = false, multiValued = false)
    private String ndTllString = null;

    @Option(name = "--tcpSrc", description = "Source TCP Port",
            required = false, multiValued = false)
    private String srcTcpString = null;

    @Option(name = "--tcpDst", description = "Destination TCP Port",
            required = false, multiValued = false)
    private String dstTcpString = null;

    @Option(name = "--extHdr", description = "IPv6 Extension Header Pseudo-field",
            required = false, multiValued = true)
    private List<String> extHdrStringList = null;

    @Option(name = "-a", aliases = "--appId", description = "Application Id",
            required = false, multiValued = false)
    private String appId = null;

    @Option(name = "-k", aliases = "--key", description = "Intent Key",
            required = false, multiValued = false)
    private String intentKey = null;


    // Treatments
    @Option(name = "--setEthSrc", description = "Rewrite Source MAC Address",
            required = false, multiValued = false)
    private String setEthSrcString = null;

    @Option(name = "--setEthDst", description = "Rewrite Destination MAC Address",
            required = false, multiValued = false)
    private String setEthDstString = null;

    @Option(name = "--setIpSrc", description = "Rewrite Source IP Address",
            required = false, multiValued = false)
    private String setIpSrcString = null;

    @Option(name = "--setIpDst", description = "Rewrite Destination IP Address",
            required = false, multiValued = false)
    private String setIpDstString = null;

    @Option(name = "--setVlan", description = "Rewrite VLAN ID",
            required = false, multiValued = false)
    private String setVlan = null;

    @Option(name = "--popVlan", description = "Pop VLAN Tag",
            required = false, multiValued = false)
    private boolean popVlan = false;

    @Option(name = "--pushVlan", description = "Push VLAN ID",
            required = false, multiValued = false)
    private String pushVlan = null;

    @Option(name = "--setQueue", description = "Set Queue ID (for OpenFlow 1.0, " +
            "also the port has to be specified, i.e., <port>/<queue>",
            required = false, multiValued = false)
    private String setQueue = null;

    // Priorities
    @Option(name = "-p", aliases = "--priority", description = "Priority",
            required = false, multiValued = false)
    private int priority = Intent.DEFAULT_INTENT_PRIORITY;

    // Constraints
    @Option(name = "-b", aliases = "--bandwidth", description = "Bandwidth",
            required = false, multiValued = false)
    private String bandwidthString = null;

    @Option(name = "--partial", description = "Allow partial installation",
            required = false, multiValued = false)
    private boolean partial = false;

    @Option(name = "-e", aliases = "--encapsulation", description = "Encapsulation type",
            required = false, multiValued = false)
    private String encapsulationString = null;


    /**
     * Constructs a traffic selector based on the command line arguments
     * presented to the command.
     * @return traffic selector
     */
    protected TrafficSelector buildTrafficSelector() {
        IpPrefix srcIpPrefix = null;
        IpPrefix dstIpPrefix = null;

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();

        if (!isNullOrEmpty(srcIpString)) {
            srcIpPrefix = IpPrefix.valueOf(srcIpString);
            if (srcIpPrefix.isIp4()) {
                selectorBuilder.matchIPSrc(srcIpPrefix);
            } else {
                selectorBuilder.matchIPv6Src(srcIpPrefix);
            }
        }

        if (!isNullOrEmpty(dstIpString)) {
            dstIpPrefix = IpPrefix.valueOf(dstIpString);
            if (dstIpPrefix.isIp4()) {
                selectorBuilder.matchIPDst(dstIpPrefix);
            } else {
                selectorBuilder.matchIPv6Dst(dstIpPrefix);
            }
        }

        if ((srcIpPrefix != null) && (dstIpPrefix != null) &&
            (srcIpPrefix.version() != dstIpPrefix.version())) {
            // ERROR: IP src/dst version mismatch
            throw new IllegalArgumentException(
                        "IP source and destination version mismatch");
        }

        //
        // Set the default EthType based on the IP version if the matching
        // source or destination IP prefixes.
        //
        Short ethType = null;
        if ((srcIpPrefix != null) && srcIpPrefix.isIp6()) {
            ethType = EthType.IPV6.value();
        }
        if ((dstIpPrefix != null) && dstIpPrefix.isIp6()) {
            ethType = EthType.IPV6.value();
        }
        if (!isNullOrEmpty(ethTypeString)) {
            ethType = EthType.parseFromString(ethTypeString);
        }
        if (ethType != null) {
            selectorBuilder.matchEthType(ethType);
        }
        if (!isNullOrEmpty(vlanString)) {
            selectorBuilder.matchVlanId(VlanId.vlanId(Short.parseShort(vlanString)));
        }
        if (!isNullOrEmpty(srcMacString)) {
            selectorBuilder.matchEthSrc(MacAddress.valueOf(srcMacString));
        }

        if (!isNullOrEmpty(dstMacString)) {
            selectorBuilder.matchEthDst(MacAddress.valueOf(dstMacString));
        }

        if (!isNullOrEmpty(ipProtoString)) {
            short ipProtoShort = IpProtocol.parseFromString(ipProtoString);
            selectorBuilder.matchIPProtocol((byte) ipProtoShort);
        }

        if (!isNullOrEmpty(fLabelString)) {
            selectorBuilder.matchIPv6FlowLabel(Integer.parseInt(fLabelString));
        }

        if (!isNullOrEmpty(icmp6TypeString)) {
            byte icmp6Type = Icmp6Type.parseFromString(icmp6TypeString);
            selectorBuilder.matchIcmpv6Type(icmp6Type);
        }

        if (!isNullOrEmpty(icmp6CodeString)) {
            byte icmp6Code = Icmp6Code.parseFromString(icmp6CodeString);
            selectorBuilder.matchIcmpv6Code(icmp6Code);
        }

        if (!isNullOrEmpty(ndTargetString)) {
            selectorBuilder.matchIPv6NDTargetAddress(Ip6Address.valueOf(ndTargetString));
        }

        if (!isNullOrEmpty(ndSllString)) {
            selectorBuilder.matchIPv6NDSourceLinkLayerAddress(MacAddress.valueOf(ndSllString));
        }

        if (!isNullOrEmpty(ndTllString)) {
            selectorBuilder.matchIPv6NDTargetLinkLayerAddress(MacAddress.valueOf(ndTllString));
        }

        if (!isNullOrEmpty(srcTcpString)) {
            selectorBuilder.matchTcpSrc(TpPort.tpPort(Integer.parseInt(srcTcpString)));
        }

        if (!isNullOrEmpty(dstTcpString)) {
            selectorBuilder.matchTcpDst(TpPort.tpPort(Integer.parseInt(dstTcpString)));
        }

        if (extHdrStringList != null) {
            short extHdr = 0;
            for (String extHdrString : extHdrStringList) {
                extHdr = (short) (extHdr | ExtHeader.parseFromString(extHdrString));
            }
            selectorBuilder.matchIPv6ExthdrFlags(extHdr);
        }

        return selectorBuilder.build();
    }

    /**
     * Generates a traffic treatment for this intent based on command line
     * arguments presented to the command.
     *
     * @return traffic treatment
     */
    protected TrafficTreatment buildTrafficTreatment() {
        final TrafficTreatment.Builder treatmentBuilder = builder();
        boolean emptyTreatment = true;

        if (!isNullOrEmpty(setEthSrcString)) {
            treatmentBuilder.setEthSrc(MacAddress.valueOf(setEthSrcString));
            emptyTreatment = false;
        }

        if (!isNullOrEmpty(setEthDstString)) {
            treatmentBuilder.setEthDst(MacAddress.valueOf(setEthDstString));
            emptyTreatment = false;
        }

        if (!isNullOrEmpty(setIpSrcString)) {
            treatmentBuilder.setIpSrc(IpAddress.valueOf(setIpSrcString));
            emptyTreatment = false;
        }

        if (!isNullOrEmpty(setIpDstString)) {
            treatmentBuilder.setIpDst(IpAddress.valueOf(setIpDstString));
            emptyTreatment = false;
        }
        if (!isNullOrEmpty(setVlan)) {
            treatmentBuilder.setVlanId(VlanId.vlanId(Short.parseShort(setVlan)));
            emptyTreatment = false;
        }
        if (popVlan) {
            treatmentBuilder.popVlan();
            emptyTreatment = false;
        }
        if (!isNullOrEmpty(pushVlan)) {
            treatmentBuilder.pushVlan();
            treatmentBuilder.setVlanId(VlanId.vlanId(Short.parseShort(pushVlan)));
            emptyTreatment = false;
        }
        if (!isNullOrEmpty(setQueue)) {
            // OpenFlow 1.0 notation (for ENQUEUE): <port>/<queue>
            if (setQueue.contains("/")) {
                String[] queueConfig = setQueue.split("/");
                PortNumber port = PortNumber.portNumber(Long.parseLong(queueConfig[0]));
                long queueId = Long.parseLong(queueConfig[1]);
                treatmentBuilder.setQueue(queueId, port);
            } else {
                treatmentBuilder.setQueue(Long.parseLong(setQueue));
            }
            emptyTreatment = false;
        }

        if (emptyTreatment) {
            return DefaultTrafficTreatment.emptyTreatment();
        } else {
            return treatmentBuilder.build();
        }
    }

    /**
     * Builds the constraint list for this command based on the command line
     * parameters.
     *
     * @return List of constraint objects describing the constraints requested
     */
    protected List<Constraint> buildConstraints() {
        final List<Constraint> constraints = new LinkedList<>();
	
	// bandwidthString="2000000";
	
        // Check for a bandwidth specification
        if (!isNullOrEmpty(bandwidthString)) {
            Bandwidth bandwidth;
            try {
                bandwidth = Bandwidth.bps(Long.parseLong(bandwidthString));
            // when the string can't be parsed as long, then try to parse as double
            } catch (NumberFormatException e) {
                bandwidth = Bandwidth.bps(Double.parseDouble(bandwidthString));
            }
            constraints.add(new BandwidthConstraint(bandwidth));
        }

        // Check for partial failure specification
        if (partial) {
            constraints.add(new PartialFailureConstraint());
        }

        // Check for encapsulation specification
        if (!isNullOrEmpty(encapsulationString)) {
            final EncapsulationType encapType = EncapsulationType.valueOf(encapsulationString);
            constraints.add(new EncapsulationConstraint(encapType));
        }

        return constraints;
    }

    @Override
    protected ApplicationId appId() {
        ApplicationId appIdForIntent;
        if (appId == null) {
            appIdForIntent = super.appId();
        } else {
            CoreService service = get(CoreService.class);
            appIdForIntent = service.getAppId(appId);
        }
        return appIdForIntent;
    }

    /**
     * Creates a key for an intent based on command line arguments.  If a key
     * has been specified, it is returned.  If no key is specified, null
     * is returned.
     *
     * @return intent key if specified, null otherwise
     */
    protected Key key() {
        Key key = null;
        ApplicationId appIdForIntent;

        if (intentKey != null) {
            key = Key.of(intentKey, appId());
        }
        return key;
    }

    /**
     * Gets the priority to use for the intent.
     *
     * @return priority
     */
    protected int priority() {
        return priority;
    }




 @Override
    protected void execute() {

	

        CoreService coreService = get(CoreService.class);
        DeviceService deviceService = get(DeviceService.class);
        FlowRuleService service = get(FlowRuleService.class);
	

	DevicesListCommand dlc=new DevicesListCommand();

	//links
	LinkService service_one = get(LinkService.class);
        Iterable<Link> links = uri != null ?
                service_one.getDeviceLinks(deviceId(uri)) : service_one.getLinks();
         
        compilePredicate();

        SortedMap<Device, List<FlowEntry>> flows = getSortedFlows(deviceService, service, coreService);

        if (outputJson()) {
	    //print("%s", json_one(this, links));		
            //print("%s", json(flows.keySet(), flows));
        } else {
            //flows.forEach((device, flow) -> printFlows(device, flow, coreService));
	    for (Link link : links) {
                //print(linkString(link));		
		//print("%s/%s",link.src().deviceId(),link.src().port());	
		
		print("%s",link.dst().deviceId());
		String lnkdst= String.valueOf(link.dst().deviceId());
		if(lnkdst.equals("of:0000000000000012")) 		
		{
		print("%s",link.dst().deviceId());	
		String portvalue = String.valueOf(link.dst().port());	
		long packets = packetstransfered(link.dst().deviceId(),deviceService.getPortStatistics(link.dst().deviceId()),portvalue);
		  String pckstr = String.valueOf(packets);		
		  print("%s",pckstr);			
		  if(packets >= 1250)
		  {
			String portentry =String.valueOf(link.src().port());
			String egress=link.src().deviceId()+"/"+4;
      			
			//if(link.src().deviceId())
			String ingress_1 = link.src().deviceId()+"/"+1;
			String ingress_2 = link.src().deviceId()+"/"+2;
			bandwidth_condition(ingress_1,egress);
			bandwidth_condition(ingress_2,egress);
	
			}
			
					
		
		}


		
		
            }
        }






	   HostService hs = get(HostService.class);
            for (Host host : hs.getHosts()) {
                //print("%s",host.id());			//populate row to print the details in a row table
            }

		
		
	
	for (Device d : dlc.getSortedDevices(deviceService)) {
	printPortStats(d.id(), deviceService.getPortStatistics(d.id()));
	}

	
	
    }



	private void bandwidth_condition(String ingressDeviceString, String egressDeviceString)
	{
	IntentService service_two = get(IntentService.class);

        ConnectPoint ingress = ConnectPoint.deviceConnectPoint(ingressDeviceString);

        ConnectPoint egress = ConnectPoint.deviceConnectPoint(egressDeviceString);

        TrafficSelector selector = buildTrafficSelector();
        TrafficTreatment treatment = buildTrafficTreatment();

        List<Constraint> constraints = buildConstraints();
        if (backup) {
            constraints.add(protection());
        }

        Intent intent = PointToPointIntent.builder()
                .appId(appId())
                .key(key())
                .selector(selector)
                .treatment(treatment)
                .ingressPoint(ingress)
                .egressPoint(egress)
                .constraints(constraints)
                .priority(priority())
                .build();
        service_two.submit(intent);
        print("Point to point intent submitted:\n%s", intent.toString());
      }
     



 /**
     * Produces a JSON array of flows grouped by the each device.
     *
     * @param devices     collection of devices to group flow by
     * @param flows       collection of flows per each device
     * @return JSON array
     */
    private JsonNode json(Iterable<Device> devices,
                          Map<Device, List<FlowEntry>> flows) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        for (Device device : devices) {
            result.add(json(mapper, device, flows.get(device)));
        }
        return result;
    }

	 /**
     * Produces a JSON array containing the specified links.
     *
     * @param context context to use for looking up codecs
     * @param links collection of links
     * @return JSON array
     */

	public static JsonNode json_one(AbstractShellCommand context, Iterable<Link> links) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();

        links.forEach(link -> result.add(context.jsonForEntity(link, Link.class)));

        return result;
    }

	/**
     * Produces a JSON object for the specified link.
     *
     * @param context context to use for looking up codecs
     * @param link   link to encode
     * @return JSON object
     */
    public static ObjectNode json_one(AbstractShellCommand context, Link link) {
         return context.jsonForEntity(link, Link.class);
    }


/**
     * Returns a formatted string representing the given link.
     *
     * @param link infrastructure link
     * @return formatted link string
     */
    public static String linkString(Link link) {
        return String.format(FMT, link.src().deviceId(), link.src().port(),
                             link.dst().deviceId(), link.dst().port(),
                             link.type(), link.state(),
                             annotations(link.annotations()),
                             link.isExpected());
    }

    /**
     * Returns a compact string representing the given link.
     *
     * @param link infrastructure link
     * @return formatted link string
     */
    public static String compactLinkString(Link link) {
        return String.format(COMPACT, link.src().deviceId(), link.src().port(),
                             link.dst().deviceId(), link.dst().port());
    }

    /**
     * Compiles a predicate to find matching flows based on the command
     * arguments.
     */
    private void compilePredicate() {
        if (state != null && !state.equals(ANY)) {
            final FlowEntryState feState = FlowEntryState.valueOf(state.toUpperCase());
            predicate = predicate.and(f -> f.state().equals(feState));
        }

        if (table != null) {
            final int tableId = Integer.parseInt(table);
            predicate = predicate.and(f -> f.tableId() == tableId);
        }
    }

    // Produces JSON object with the flows of the given device.
    private ObjectNode json(ObjectMapper mapper,
                            Device device, List<FlowEntry> flows) {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode array = mapper.createArrayNode();

        flows.forEach(flow -> array.add(jsonForEntity(flow, FlowEntry.class)));

        result.put("device", device.id().toString())
                .put("flowCount", flows.size())
                .set("flows", array);
        return result;
    }

    /**
     * Returns the list of devices sorted using the device ID URIs.
     *
     * @param deviceService device service
     * @param service flow rule service
     * @param coreService core service
     * @return sorted device list
     */
    private SortedMap<Device, List<FlowEntry>> getSortedFlows(DeviceService deviceService,
                                                          FlowRuleService service, CoreService coreService) {
        SortedMap<Device, List<FlowEntry>> flows = new TreeMap<>(Comparators.ELEMENT_COMPARATOR);
        List<FlowEntry> rules;

        Iterable<Device> devices = null;
        if (uri == null) {
            devices = deviceService.getDevices();
        } else {
            Device dev = deviceService.getDevice(DeviceId.deviceId(uri));
            devices = (dev == null) ? deviceService.getDevices()
                                    : Collections.singletonList(dev);
        }

        for (Device d : devices) {
            if (predicate.equals(TRUE_PREDICATE)) {
                rules = newArrayList(service.getFlowEntries(d.id()));
            } else {
                rules = newArrayList();
                for (FlowEntry f : service.getFlowEntries(d.id())) {
                    if (predicate.test(f)) {
                        rules.add(f);
                    }
                }
            }
            rules.sort(Comparators.FLOW_RULE_COMPARATOR);

            if (suppressCoreOutput) {
                short coreAppId = coreService.getAppId("org.onosproject.core").id();
                rules = rules.stream()
                        .filter(f -> f.appId() != coreAppId)
                        .collect(Collectors.toList());
            }
            flows.put(d, rules);
        }
        return flows;
    }

    /**
     * Prints flows.
     *
     * @param d     the device
     * @param flows the set of flows for that device
     * @param coreService core system service
     */
    private void printFlows(Device d, List<FlowEntry> flows,
                              CoreService coreService) {
        boolean empty = flows == null || flows.isEmpty();
        //print("deviceId=%s, flowRuleCount=%d", d.id(), empty ? 0 : flows.size());
        if (empty || countOnly) {
            return;
        }

        for (FlowEntry f : flows) {
            if (shortOutput) {
                //print(SHORT_FORMAT, f.bytes(), f.packets());//Long.toHexString(f.id().value())
            }else {
                //print(SHORT_FORMAT, f.bytes(), f.packets());
            }
        }
    }

	private long packetstransfered(DeviceId deviceId, Iterable<PortStatistics> portStats,String portvalue ){
        //print("deviceId=%s", deviceId);
        for (PortStatistics stat : sortByPort(portStats)) {
            if (portNumber != null && stat.port() != portNumber) {
                continue;
            }
		String statport = Integer.toString(stat.port());
		
            if(statport.equals(portvalue))
		{
			return stat.packetsReceived();
	       }
        }
	return 0;
      }





	
       private void printPortStats(DeviceId deviceId, Iterable<PortStatistics> portStats) {
        //print("deviceId=%s", deviceId);
        for (PortStatistics stat : sortByPort(portStats)) {
            if (portNumber != null && stat.port() != portNumber) {
                continue;
            }
            	
	     //  print(FORMAT,stat.port(), stat.packetsReceived(), stat.packetsSent(), stat.bytesReceived(),
             //    stat.bytesSent(), stat.packetsRxDropped(), stat.packetsTxDropped(), stat.durationSec());
        }
      }

  
       private static List<PortStatistics> sortByPort(Iterable<PortStatistics> portStats) {
        List<PortStatistics> portStatsList = Lists.newArrayList(portStats);
        portStatsList.sort((PortStatistics o1, PortStatistics o2) ->
                o1.port() - o2.port());
        return portStatsList;
    }
}







