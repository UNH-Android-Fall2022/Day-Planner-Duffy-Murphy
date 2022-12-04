package com.example.dayplanner

// import com.example.googlemapstest.databinding.ActivityMapsBinding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.dayplanner.databinding.ListAddMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ListAddMapBinding
    private var newMarker: Marker? = null
    private var latLng: LatLng? = null
    private var location: Location? = null
    private var title: String = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListAddMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.mapFloatingActionButton.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("loc", title)
            startActivity(intent)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker at last known location (It's not that important) and move the camera

        getCurrentLocation()

        // This listener was adapted from
        // https://stackoverflow.com/questions/31248257/androidgoogle-maps-get-the-address-of-location-on-touch
        mMap.setOnMapClickListener { point -> //save current location
            latLng = point
            var addresses: List<Address?> = ArrayList()

            // TODO: These 2 lines might cause exception. Figure out.
            val geocoder = Geocoder(binding.root.context, Locale.getDefault())
            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1)

            var address: String? = null
            if (!addresses.isNullOrEmpty()) {
                address = addresses.get(0)?.getAddressLine(0) //0 to obtain first possible address
            }

            if (address != null) {
//                val city: String = addresses.get(0)?.getLocality() ?: ""
//                val state: String = addresses.get(0)?.getAdminArea() ?: ""
//                val country: String = addresses.get(0)?.getCountryName() ?: ""
//                val postalCode: String = addresses.get(0)?.getPostalCode() ?: ""
//                title = "$address $state $postalCode"
                title = "$address"
                //remove previously placed Marker
                newMarker?.remove()

                // Replace the search string too
                binding.mapSearchText.setText(title)

                //place marker where user just clicked
                newMarker = mMap.addMarker(
                    MarkerOptions().position(point).title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                newMarker?.showInfoWindow()
            }
        }

        binding.mapSearchSubmit.setOnClickListener() {
            val geocoder = Geocoder(binding.root.context, Locale.getDefault())
            val searchString = binding.mapSearchText.text.toString()
            val addresses = geocoder.getFromLocationName(searchString, 1)
            if (!addresses.isNullOrEmpty()) {
                val lat = addresses[0].latitude
                val lon = addresses[0].longitude
                val point = LatLng(lat, lon)

                title = addresses[0].getAddressLine(0)
                // remove any old markers to avoid confusion
                newMarker?.remove()

                // Replace the search string too
                binding.mapSearchText.setText(title)

                // add a marker so the user knows what's going on
                newMarker = mMap.addMarker(
                    MarkerOptions().position(point).title(searchString)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                newMarker?.showInfoWindow()
                // Also move the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15F))
            }
        }

    }

    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Do nothing I guess
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    location = task.result
                    if (location == null) {
                        Log.d("my special tag", "Error: Null")
                    } else {
                        var coordinates = LatLng(40.0, 74.0)
//                        var lat = 40.0
//                        var lon = 74.0
                        if (location != null) {
                            coordinates = LatLng(location!!.latitude, location!!.longitude)
//                            lat = location!!.latitude
//                            lon = location!!.longitude
                        }
                        newMarker = mMap.addMarker(MarkerOptions().position(coordinates).title(""))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15F))
                    }
                }
            } else {
                Log.d("my special tag", "Turn on location")
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    // From https://www.youtube.com/watch?v=mwzKYIB9cQs
    private fun checkPermissions() : Boolean {
        return (ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.d("my special tag", "Granted")
                getCurrentLocation()
            } else {
                Log.d("my special tag", "Denied. TODO: do something if denied.")
            }
        }
    }
}