/**
 *  Copyright 2018 Iman Haryadi
 *
 *  GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 */

import physicalgraph.zigbee.zcl.DataType
metadata {
    definition (name: "ZigBee Repeater", namespace: "iharyadi", author: "iharyadi", ocfDeviceType: "oic.d.switch", minHubCoreVersion: '000.019.00012', runLocally: false, executeCommandsLocally: true) {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0006, 0B05", manufacturer: "KMPCIL", model: "RES001", deviceJoinName: "ZB Repeater"
        }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        def tiles_detail = [];
        tiles_detail.add("switch")
        MapDiagAttributes().each{ k, v -> valueTile("$v", "device.$v", width: 2, height: 2) {
        		state "val", label: "$v \n"+'${currentValue}', defaultState: true
    		};
            tiles_detail.add(v);
        }
        tiles_detail.add("refresh")
                
        main "switch"        
        details(tiles_detail)        
    }
}

private def NUMBER_OF_RESETS_ID()
{
	return 0x0000;
}

private def MAC_TX_UCAST_RETRY_ID()
{
	return 0x0104;
}

private def MAC_TX_UCAST_FAIL_ID()
{
	return 0x0105;
}

private def NWK_DECRYPT_FAILURES_ID()
{
	return 0x0115;
}

private def PACKET_VALIDATE_DROP_COUNT_ID()
{
	return 0x011A;
}

private def PARENT_COUNT_ID()
{
	return 0x011D+1;
}

private def CHILD_COUNT_ID()
{
	return 0x011D+2;
}

private def NEIGHBOR_COUNT_ID()
{
	return 0x011D+3;
}

private def LAST_RSSI_ID()
{
	return 0x011D;
}

private def DIAG_CLUSTER_ID()
{
	return 0x0B05;
}

private def TEMPERATURE_CLUSTER_ID()
{
	return 0x0403;
}

private def MapDiagAttributes()
{
	def result = [(CHILD_COUNT_ID()):'Children',
        (NEIGHBOR_COUNT_ID()):'Neighbor',
        (NUMBER_OF_RESETS_ID()):'ResetCount',
    	(MAC_TX_UCAST_RETRY_ID()):'TXRetry',
        (MAC_TX_UCAST_FAIL_ID()):'TXFail',
        (LAST_RSSI_ID()):'RSSI',
        (NWK_DECRYPT_FAILURES_ID()):'DecryptFailure',
        (PACKET_VALIDATE_DROP_COUNT_ID()):'PacketDrop'] 

	return result;
}

private def createDiagnosticEvent( String attr_name, type, value )
{
	def result = [:]
    result.name = attr_name
    result.translatable = true
    
    def converter = [(DataType.INT8):{int val -> return (byte) val},
    (DataType.INT16):{int val -> return val},
    (DataType.UINT16):{int val -> return (long)val}] 
    
    result.value = converter[zigbee.convertHexToInt(type)]( zigbee.convertHexToInt(value));
    
    result.descriptionText = "{{ device.displayName }} $attr_name was $result.value"

    return createEvent(result)
}

def parseDiagnosticEvent(def descMap)
{       
    def attr_name = MapDiagAttributes()[descMap.attrInt];
    if(!attr_name)
    {
    	return null;
  	}
    
    return createDiagnosticEvent(attr_name, descMap.encoding, descMap.value)
}

def parse(String description) {
    log.debug "description is $description"
   
    def event = zigbee.getEvent(description)
    if(event)
    {
   		log.debug "Recognized Zigbee Event"
    }
    else
    {
    	def descMap = zigbee.parseDescriptionAsMap(description)
        if(description?.startsWith("read attr"))
        {
            if(descMap?.clusterInt == DIAG_CLUSTER_ID())
            {
                event = parseDiagnosticEvent(descMap);
            }
    	}
    }
    
    event?sendEvent(event):{log.warn "DID NOT PARSE MESSAGE : $description"}
}

def off() {
    zigbee.off()
}

def on() {
    zigbee.on()
}

def refresh() {
    log.debug "Refresh"
    def cmd = zigbee.onOffRefresh()
    MapDiagAttributes().each{ k, v -> cmd +=  zigbee.readAttribute(DIAG_CLUSTER_ID(), k) }
    
    return cmd
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    return refresh();
}