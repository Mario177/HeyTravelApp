package com.example.heytravel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class MapsFragment : Fragment() {

    private lateinit var locationManager: LocationManager
    private var hasNetwork:Boolean = false
    private var hasGPS:Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        val markerHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val drinking = msg.obj as ArrayList<*>
                try {
                    googleMap.addMarker(MarkerOptions()
                        .position(drinking[2] as LatLng)
                        .title("${drinking[0]}")
                    )

                }catch (e:Exception){
                    Log.d(tag,e.toString())
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                if (currentMarker == null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    currentMarker = googleMap.addMarker(MarkerOptions()
                        .title("你在這")
                        .position(currentLatLng))
                    val targetURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=1000&keyword=景點&language=zh-TW&key=${getString(R.string.apikey)}"
                    var drinking: JSONObject
                    var drinkingArray: JSONArray
                    var anyDrink: JSONObject

                    GlobalScope.launch {

                        val client = OkHttpClient.Builder().build()
                        val request = Request.Builder().url(targetURL).build()
                        val response = client.newCall(request).execute()
                        response.body.run {
                            drinking = JSONObject(string())
                            drinkingArray = drinking["results"] as JSONArray
                            for (i in 0 until drinkingArray.length()) {
                                anyDrink = drinkingArray[i] as JSONObject
                                var drinkingGeometry = anyDrink.get("geometry") as JSONObject
                                var drinkingLocation = drinkingGeometry.get("location") as JSONObject
                                var drinkLatlng = LatLng(drinkingLocation.get("lat").toString().toDouble(),drinkingLocation.get("lng").toString().toDouble())
                                val msg = Message().apply {
                                    obj = arrayListOf(anyDrink.get("name"),anyDrink.get("vicinity"),drinkLatlng)
                                }
                                markerHandler.sendMessage(msg)
                            }
                            }
                        }
                    }

                }
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        mapFragment?.getMapAsync(callback)
        checkPermission() // 使用前先確認權限以及功能是否正常
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        handler.postDelayed(object : Runnable {
            override fun run() {
                mapFragment?.getMapAsync(callback)
                handler.postDelayed(this, 0) // 本來是避免資料尚未init後來發現不需要了哈哈但還是留下這孩子了<3
            }
        }, 0)
    }
    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { // 進行權限檢查
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),1)
        } // 若權限檢查未通過則要求權限
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!(hasGPS || hasNetwork)){
            Toast.makeText(requireActivity(), "請檢查網路或位置服務是否開啟", Toast.LENGTH_LONG).show()
        }
    }
}