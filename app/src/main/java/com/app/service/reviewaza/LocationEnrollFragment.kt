package com.app.service.reviewaza

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.RoundedCorner
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.app.service.reviewaza.call.Key.Companion.DB_USERS
import com.app.service.reviewaza.databinding.FragmentLocationEnrollBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LocationEnrollFragment :
    BaseFragment<FragmentLocationEnrollBinding>(R.layout.fragment_location_enroll),
    OnMapReadyCallback, OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var trackingUserId: String = ""
    private val markerMap = hashMapOf<String, Marker>()

    lateinit var mainActivity: MainActivity

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        when {
            permission.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permission.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                // TODO 설정으로 보내기 or 교육용 팝업 띄워서 다시 권한 요청하기
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            // 새로 요청된 위치 정보
            for (location in locationResult.locations) {

                Log.e(
                    "MapActivity",
                    "onLocationResult : ${location.latitude} ${location.longitude}"
                )

                val uid = Firebase.auth.currentUser?.uid.orEmpty()

                val locationMap = mutableMapOf<String, Any>()
                locationMap["latitude"] = location.latitude
                locationMap["longitude"] = location.longitude
                Firebase.database.reference.child(DB_USERS).child(uid).updateChildren(locationMap)
            }
        }
    }

    override fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestLocationPermission()
        initView()
    }

    private fun initView() {

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
                ?: SupportMapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().add(R.id.mapFragment, it).commit()
                }
        mapFragment.getMapAsync(this)
        setupCuurentLocation()
        setupFirebaseDatabase()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @UiThread
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setMaxZoomPreference(20.0f)
        googleMap.setMinZoomPreference(10.0f)

        googleMap.setOnMapClickListener {
            trackingUserId = ""
        }

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        trackingUserId = marker.tag as? String ?: ""
        return false
    }

    private fun getCurrentLocation() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5 * 1000).build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 권한이 있는 상태
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        moveLastLocation()
    }

    private fun setupCuurentLocation() {
        binding.currentLocationButton.setOnClickListener {
            trackingUserId = ""
            moveLastLocation()
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun moveLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16.0f)
            )
        }
    }

    private fun setupFirebaseDatabase() {

        if (Firebase.auth.currentUser != null) {
            Firebase.database.reference.child(DB_USERS)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val user = snapshot.getValue(UserItem::class.java) ?: return
                        val uid = user.userId ?: return

                        if (markerMap[uid] == null) {
                            markerMap[uid] = makeNewMarker(user, uid) ?: return
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val user = snapshot.getValue(UserItem::class.java)
                        val uid = user?.userId ?: return
                        Log.e("dataChangeCheck", "user: $user uid: $uid")
                        if (markerMap[uid] == null) {
                            markerMap[uid] = makeNewMarker(user, uid) ?: return
                        } else {

                            markerMap[uid]?.position =
                                LatLng(user.latitude ?: 0.0, user.longitude ?: 0.0)
                        }

                        if (uid == trackingUserId) {
                            Log.e("trackingUserId", "uid: $uid, tracingId: ${trackingUserId}")
                            googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(LatLng(user.latitude ?: 0.0, user.longitude ?: 0.0))
                                        .zoom(16.0f)
                                        .build()
                                )
                            )
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {}

                })
        } else {
            Toast.makeText(requireContext(), "로그인을 해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeNewMarker(user: UserItem, uid: String): Marker? {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(user.latitude ?: 0.0, user.longitude ?: 0.0))
                .title(user.username.orEmpty())
        ) ?: return null

        marker.tag = uid

        Log.e("userImage", "${user.userImage}")

        if(user.userImage == null) {
            Glide.with(this).asBitmap()
                .load(R.drawable.ic_baseline_person_24)
                .transform(RoundedCorners(60))
                .override(200)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            mainActivity.runOnUiThread {
                                marker.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        resource
                                    )
                                )
                            }
                        }
                        return true
                    }

                }).submit()
        } else {
            Glide.with(this).asBitmap()
                .load(user.userImage)
                .transform(RoundedCorners(60))
                .override(200)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            mainActivity.runOnUiThread {
                                marker.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        resource
                                    )
                                )
                            }
                        }
                        return true
                    }

                }).submit()

        }

        return marker
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}