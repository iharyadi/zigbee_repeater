import physicalgraph.zigbee.zcl.DataType
metadata {
    definition (name: "Water Sensor", namespace: "iharyadi", author: "iharyadi") {
    	capability "Water Sensor"
        capability "Sensor"
    }
        
    tiles(scale: 2) {
       multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", label: 'Dry', icon: "st.alarm.water.dry", backgroundColor: "#ffffff"
				attributeState "wet", label: 'Wet', icon: "st.alarm.water.wet", backgroundColor: "#00A0DC"
			}
		}
        main (["water"])
        details(["water"])
    }
    
    preferences {
        section("setup")
           {
                input name:"threshold", type:"decimal", title: "Threshold", description: "Threshold voltage which considered as dry",
                    range: "0..3.3", displayDuringSetup: false, defaultValue: 2.0
           }
    }
    // simulator metadata
    simulator {
    }
        
    preferences {    
    }
}

def parse(String description) { 

	def event;
    
    if(!description?.startsWith("read attr - raw:"))
    {
        return event;
    }
    
    def descMap = zigbee.parseDescriptionAsMap(description)
    
    def adc;
    def vdd;

	if(descMap.attrInt?.equals(0x0103))
    {
    	adc = descMap.value
    }
    else if (descMap.attrInt?.equals(0x0104))
    {
        vdd = descMap.value
    }
    else
    {   
        adc = descMap.additionalAttrs?.find { item -> item.attrInt?.equals(0x0103)}?.value
	}
    
    if(vdd)
    {
    	state.lastVdd = (((float)zigbee.convertHexToInt(vdd)*3.45)/0x1FFF)
    }  
    
    if(!adc)
    {
    	return event   
    }
    
    if(!state.lastVdd)
    {
    	return event
    }

    float volt = 0;
   	volt = (zigbee.convertHexToInt(adc) * state.lastVdd)/0x1FFF
     
    event = createEvent(name:"water",value:volt > threshold? "dry":"wet")
    return event;
}

def configure_child() {

	def cmds = []
    cmds = cmds + parent.writeAttribute(0x000C, 0x00105, DataType.UINT32, 250)
   	cmds = cmds + parent.configureReporting(0x000C, 0x0103, DataType.UINT16, 0, 306,25)
	return cmds
}

def installed() {
}

def updated() {
	cmds = cmds + parent.readAttribute(0x000C, 0x0103, DataType.UINT16)
	return cmds
}