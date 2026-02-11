package com.example.busradar4

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import BusResponse
import Bus
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.io.Console
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private lateinit var refreshRunnable: Runnable
    private val REFRESH_INTERVAL = 7000L

    private var lastBusList: List<Bus> = emptyList()

    private val visibleMarkers = mutableMapOf<String, Marker>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.um.warszawa.pl/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var ztmService = retrofit.create(ZtmApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.um.warszawa.pl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        ztmService = retrofit.create(ZtmApi::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        refreshRunnable = object : Runnable {
            override fun run() {
                Log.d("BusRadar", "Automatyczne odświeżanie danych...")
                downloadBusPositions()

                handler.postDelayed(this, REFRESH_INTERVAL)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap

        val warszawa = LatLng(52.2297, 21.0122)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warszawa, 14f))

        downloadBusPositions()

        mMap.setOnCameraIdleListener {
            redrawVisibleBuses()
        }
    }


    private fun downloadBusPositions() {
        ztmService.getBuses(apiKey = "bcab0f4f-96c6-47bf-9ba4-5714732db582").enqueue(object : Callback<BusResponse> {

            override fun onResponse(call: Call<BusResponse>, response: Response<BusResponse>) {
                if (response.isSuccessful) {
                    lastBusList = response.body()?.result ?: emptyList()
                    redrawVisibleBuses()
                }
            }

            override fun onFailure(call: Call<BusResponse>, t: Throwable) {
                Log.e("BusRadar", "Błąd sieci: ${t.message}")
            }
        })
    }

    private fun createCustomMarker(line: String, bearing: Float): BitmapDescriptor {
        val markerView = LayoutInflater.from(this).inflate(R.layout.bus_marker, null)
        val textView = markerView.findViewById<TextView>(R.id.busLineText)
        val arrowView = markerView.findViewById<TextView>(R.id.directionArrow)

        textView.text = line

        // rotacja strzałki w znaczniku autobusu
        if (bearing != 0f) {
            arrowView.visibility = View.VISIBLE
            arrowView.rotation = bearing - 90f
        } else {
            arrowView.visibility = View.GONE
        }

        // rysowanie mapy
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun isBusVisible(busLatLng: LatLng): Boolean {
        val bounds = mMap.projection.visibleRegion.latLngBounds
        return bounds.contains(busLatLng)
    }

    private fun redrawVisibleBuses() {
        if (!::mMap.isInitialized) return

        val tooManyResultsView = findViewById<TextView>(R.id.tooManyResultsText)
        val bounds = mMap.projection.visibleRegion.latLngBounds

        // selekcja autobusów
        val busesInView = lastBusList.filter { bus ->
            val pos = LatLng(bus.Lat.toDouble(), bus.Lon.toDouble())
            val isVisible = bounds.contains(pos)
            val isFresh = isTimeFresh(bus.Time)

            isVisible && isFresh
        }

        if (busesInView.size > 150) {
            // czyszczenie markerów po oddaleniu
            visibleMarkers.forEach { it.value.remove() }
            visibleMarkers.clear()

            tooManyResultsView.visibility = View.VISIBLE
            return
        } else {
            tooManyResultsView.visibility = View.GONE
        }

        // usuwanie starych markerow
        val currentApiVehicleIds = busesInView.map { "${it.Lines}_${it.VehicleNumber}".trim() }.toSet()
        val iter = visibleMarkers.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (!currentApiVehicleIds.contains(entry.key)) {
                entry.value.remove()
                iter.remove()
            }
        }

        // aktualizacja pozycji
        for (bus in busesInView) {
            val combinedId = "${bus.Lines}_${bus.VehicleNumber}".trim()
            val position = LatLng(bus.Lat.toDouble(), bus.Lon.toDouble())
            val existingMarker = visibleMarkers[combinedId]

            // Obliczanie kierunku poruszania sie
            var bearing = existingMarker?.tag as? Float ?: 0f
            if (existingMarker != null) {
                val oldPos = existingMarker.position
                if (oldPos.latitude != position.latitude || oldPos.longitude != position.longitude) {
                    bearing = calculateBearing(oldPos, position)
                }
                existingMarker.position = position
                existingMarker.setIcon(createCustomMarker(bus.Lines, bearing))
                existingMarker.tag = bearing
            } else {
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .icon(createCustomMarker(bus.Lines, 0f))
                        .anchor(0.5f, 0.5f)
                )
                marker?.tag = 0f
                if (marker != null) visibleMarkers[combinedId] = marker
            }
        }
    }

    private fun calculateBearing(oldPos: LatLng, newPos: LatLng): Float {
        val lat1 = Math.toRadians(oldPos.latitude)
        val lon1 = Math.toRadians(oldPos.longitude)
        val lat2 = Math.toRadians(newPos.latitude)
        val lon2 = Math.toRadians(newPos.longitude)

        val dLon = lon2 - lon1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)

        val radians = Math.atan2(y, x)
        return Math.toDegrees(radians).toFloat()
    }

    // sprawdzanie aktywności autobusu
    private fun isTimeFresh(busTime: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val busDate = sdf.parse(busTime)
            val now = Date()

            val diff = now.time - (busDate?.time ?: 0)

            diff < 60000
        } catch (e: Exception) {
            false
        }
    }
}