import physicalgraph.zigbee.zcl.DataType
metadata {
    definition (name: "TempProbe", namespace: "iharyadi", author: "iharyadi") {
        capability "Temperature Measurement"
        capability "Sensor"
        attribute "rawTemp", "number"
    }
}

tiles(scale: 2) {
        multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°',
                    backgroundColors: [
                        [value: 31, color: "#153591"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 95, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]
                    ]
            }
        }
        
        valueTile("rawtemp", "device.rawTemp", inactiveLabel: false, width: 3, height: 2, wordWrap: true) {
            state "rawtemp", label: '${currentValue} C', unit:"", defaultState: true
        }
        
        def tiles_detail = ["temperature", "rawtemp"];
                
        main "temperature"        
        details(tiles_detail)        
}
preferences {
    section("Temperature Adjustment")
    {
        input name:"tempIce", type:"decimal", title: "Degrees", description: "Measured Ice Temperature in Celcius",
            range: "*..*", displayDuringSetup: false
        input name:"tempBoiling", type:"decimal", title: "Degrees", description: "Measured Boiling Water Temperature in Celcius",
            range: "*..*", displayDuringSetup: false
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

    float temp = ((((((zigbee.convertHexToInt(adc)*state.lastVdd)/0x1FFF)+0.025)*2) -1.25)/0.005) -250.0
    
    float tempAdusted = temp
    
    if(tempIce && tempBoiling)
    {
    	tempAdusted = (((temp-tempIce) * 99.99) / (tempBoiling-tempIce)) + 0.01
    }
	
	def dispValue
    dispValue = String.format("%3.2f",temp)  
    sendEvent(name:"rawTemp",value:"${dispValue}", unit:"")
    
    dispValue = String.format("%3.2f",tempAdusted) 
	sendEvent(name:"temperature",value:"${dispValue}", unit:"C")
    return null;
}

def configure_child() {
def cmds = []
	cmds = cmds + parent.writeAttribute(0x000C, 0x0102, DataType.UINT16, 0)
    cmds = cmds + parent.writeAttribute(0x000C, 0x00105, DataType.UINT32, 250)
   	cmds = cmds + parent.configureReporting(0x000C, 0x0103, DataType.UINT16, 5, 60,50)
	return cmds
}

def installed() {
}