package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var location: LatLng? = null
    private var poi: PointOfInterest? = null
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.saveBtn.setOnClickListener {
            onLocationSelected()

        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

        when {
            poi != null -> {
                viewModel.selectedPOI.value = poi
                viewModel.reminderSelectedLocationStr.value = poi!!.name
                viewModel.latitude.value = poi!!.latLng.latitude
                viewModel.longitude.value = poi!!.latLng.longitude
                viewModel.navigationCommand.value = NavigationCommand.Back
            }
            location != null -> {
                viewModel.latitude.value = location!!.latitude
                viewModel.longitude.value = location!!.longitude
                viewModel.reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
                viewModel.navigationCommand.value = NavigationCommand.Back
            }
            else -> {
                Toast.makeText(context, "Please Select a location!", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        setMapStyle(map)
        enableMyLocation()
        zoomOnMe()
        setMapLongClick(map)
        setPoiClick(map)
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            this.poi = poi
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            addOverLay(poi.latLng, map)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            // A Snippet is Additional text that's displayed below the title.
            location = latLng
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            addOverLay(latLng, map)
        }

    }

    private fun addOverLay(
        latLng: LatLng?,
        map: GoogleMap
    ) {
        val overlaySize = 100f
        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.circle))
            .position(latLng, overlaySize)
        map.addGroundOverlay(androidOverlay)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    fun requestMyGpsLocation(context: Context, callback: (location: Location) -> Unit) {
        val request = LocationRequest()
        request.numUpdates = 1
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client = LocationServices.getFusedLocationProviderClient(context)

        val permission = ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val location = locationResult?.lastLocation
                    if (location != null)
                        callback.invoke(location)
                }
            }, null)
        }
    }

    fun zoomOnMe() {
        requestMyGpsLocation(requireContext()) { location ->
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude,location.longitude ), 10F ))
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Toast.makeText(context, "Style parsing failed.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(context, "Can't find style. Error: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
