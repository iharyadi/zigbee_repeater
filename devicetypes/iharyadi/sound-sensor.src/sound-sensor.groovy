import physicalgraph.zigbee.zcl.DataType
metadata {
    definition (name: "Sound Sensor", namespace: "iharyadi", author: "iharyadi") {
        capability "Sound Sensor"
        capability "Sensor"
    }
    
    tiles(scale: 2) {
       multiAttributeTile(name: "sound", type: "generic", width: 6, height: 4) {
			tileAttribute("device.sound", key: "PRIMARY_CONTROL") {
				attributeState "detected", label: 'detected', icon: "st.security.alarm.on", backgroundColor: "#00A0DC"
				attributeState "not detected", label: 'not detected', icon: "st.security.alarm.off", backgroundColor: "#cccccc"
			}
		}
        main (["sound"])
        details(["sound"])
    }

    // simulator metadata
    simulator {
    }
        
    
    preferences {    
    }
}

def soundNotDetected()
{
	return sendEvent(name:"sound", value:"not detected")    
}

def parse(String description) {  
    if(!description?.startsWith("read attr - raw:"))
    {
        return null;
    }
    
    def descMap = zigbee.parseDescriptionAsMap(description)
    
    def value = descMap.attrInt?.equals(0x0055) ? 
        descMap.value : 
    	descMap.additionalAttrs?.find { item -> item.attrInt?.equals(0x0055)}.value
    
    if(!value)
    {
        return null
    }
    
    runIn(20,soundNotDetected)
    return createEvent(name:"sound", value:"detected")
}

def installed() 
{ 
}

def configure_child()
{
	def cmds = []
	cmds = cmds + parent.writeAttribute(0x000F, 0x0101, DataType.UINT32, 0)
    cmds = cmds + parent.configureReporting(0x000F, 0x0055, DataType.BOOLEAN, 17, 0xFFFE,1)
    log.debug "configure_child $cmds"
	return cmds
}