data class BusResponse(val result: List<Bus>)

data class Bus(
    val Lines: String,
    val Lat: String,
    val Lon: String,
    val VehicleNumber: String,
    val Time: String,
)