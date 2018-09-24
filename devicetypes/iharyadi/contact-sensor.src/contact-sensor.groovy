import physicalgraph.zigbee.zcl.DataType
metadata {
    definition (name: "Contact Sensor", namespace: "iharyadi", author: "iharyadi") {
    	capability "Configuration"
        capability "Contact Sensor"
        capability "Sensor"
    }
        
    tiles(scale: 2) {
        multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
            tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
                attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
                attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
            }
        }
        main (["status"])
        details(["status"])
    }

    // simulator metadata
    simulator {
    }
        
    
    preferences {    
    }
}

def parse(String description) { 
    if(!description?.startsWith("read attr - raw:"))
    {
        return null;
    }
    
    def descMap = zigbee.parseDescriptionAsMap(description)
        
    def val = descMap.attrInt?.equals(0x0055) ? 
        descMap.value : 
    	descMap.additionalAttrs?.find { item -> item.attrInt?.equals(0x0055)}.value
    
    if(!val)
    {
        return null
    }

	if(val == "00")
    {
    	return createEvent(name:"contact",value:"closed")
    }
 	
    return createEvent(name:"contact",value:"open")
}

def configure_child() {
    def cmds = []
    cmds = cmds + parent.writeAttribute(0x000F, 0x0101, DataType.UINT32, 25)
    cmds = cmds + parent.configureReporting(0x000F, 0x0055, DataType.BOOLEAN, 0, 500,1)
    return cmds
}

def installed() {
}

def updated() {
    log.debug "updated():"
}