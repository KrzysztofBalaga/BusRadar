import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ZtmApi {
    @GET("api/action/busestrams_get/")
    fun getBuses(
        @Query("resource_id") resourceId: String = "f2e5503e-927d-4ad3-9500-4fa9e557d969",
        @Query("apikey") apiKey: String = "bcab0f4f-96c6-47bf-9ba4-5714732db582",
        @Query("type") type: Int = 1
    ): Call<BusResponse>
}