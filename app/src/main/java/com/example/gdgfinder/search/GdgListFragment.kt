package com.example.gdgfinder.search

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gdgfinder.databinding.FragmentGdgListBinding
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar


private const val LOCATION_PERMISSION_REQUEST = 1
private const val LOCATION_PERMISSION = "android.permission.ACCESS.FINE.LOCATION"

class GdgListFragment: Fragment() {

    private val viewModel: GdgListViewModel by lazy {
        ViewModelProvider(this).get(GdgListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentGdgListBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = GdgListAdapter(GdgClickListener { chapter ->
            val destination = Uri.parse(chapter.website)
            startActivity(Intent(Intent.ACTION_VIEW, destination))
        })

        binding.gdgChapterList.adapter = adapter

        viewModel.showNeedLocation.observe(viewLifecycleOwner, object: Observer<Boolean>{
            override fun onChanged(show: Boolean?) {
                if(show == true){
                    Snackbar.make(
                        binding.root,
                        "No location, Enable location in settings (hint: Test with Maps) then check app permissions!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLastLocationOrStartLocationUpdates()
    }

    private fun requestLocationPermission(){
        requestPermissions(arrayOf(LOCATION_PERMISSION), LOCATION_PERMISSION_REQUEST)
    }

    private fun requestLastLocationOrStartLocationUpdates(){
        if (ContextCompat.checkSelfPermission(requireContext(), LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null){
                startLocationUpdates(fusedLocationClient)
            } else {
                viewModel.onLocationUpdate(location)
            }
        }
    }

    private fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient){
        if(ContextCompat.checkSelfPermission(requireContext(), LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission()
            return
        }

        val request = LocationRequest().setPriority(LocationRequest.PRIORITY_LOW_POWER)
        val callback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                val location = locationResult?.lastLocation ?: return
                viewModel.onLocationUpdate(location)
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            LOCATION_PERMISSION_REQUEST -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    requestLastLocationOrStartLocationUpdates()
                }
            }
        }
    }

}