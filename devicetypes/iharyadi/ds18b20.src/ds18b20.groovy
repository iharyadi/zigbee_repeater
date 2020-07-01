import physicalgraph.zigbee.zcl.DataType
metadata {
    definition (name: "DS18B20", namespace: "iharyadi", author: "iharyadi") {
        capability "Temperature Measurement"
        capability "Sensor"
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
         
        def tiles_detail = ["temperature"];
                
        main "temperature"        
        details(tiles_detail)        
}

preferences {
}

private float byteArrayToFloat(def byteArray) {
    int intBits = 
      (zigbee.convertHexToInt(byteArray[3]) & 0xFF) << 24 | (zigbee.convertHexToInt(byteArray[2]) & 0xFF) << 16 | (zigbee.convertHexToInt(byteArray[1]) & 0xFF) << 8 | (zigbee.convertHexToInt(byteArray[0]) & 0xFF);
    return Float.intBitsToFloat(intBits);  
}

def parse(def data) { 
	def event;
    
    float dispValue = (float) byteArrayToFloat(data[2..5]).round(2);
    
    def result = [:]
    result.name = "temperature"
    result.unit = "°${location.temperatureScale}"
    
    result.value = convertTemperatureIfNeeded(dispValue,"C",1) 
    result.descriptionText = "${device.displayName} ${result.name} is ${result.value} ${result.unit}"
    
    return createEvent(result);
}

def configure_child() {
}

def installed() {
}