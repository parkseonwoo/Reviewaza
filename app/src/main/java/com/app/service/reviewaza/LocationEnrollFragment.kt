package com.app.service.reviewaza

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import com.app.service.reviewaza.databinding.FragmentLocationEnrollBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import java.io.IOException
import java.util.*

class LocationEnrollFragment : BaseFragment<FragmentLocationEnrollBinding>(R.layout.fragment_location_enroll),
    OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    override fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestPermissions()
        initView()
    }

    private fun initView() {
        // 네이버맵 동적으로 불러오기
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.naver_map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.naver_map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    // 위치권한 관련 요청
    private fun requestPermissions() {
        // 내장 위치 추적 기능 사용
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 맵 위치 권한 설정을 확인
        if (Build.VERSION.SDK_INT >= 23) {

            TedPermission.create()
                .setPermissionListener(object: PermissionListener {
                    override fun onPermissionGranted() {
                        Log.e("위치 권한 부여 ", "성공")
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        TODO("Not yet implemented")
                    }

                })
                .setRationaleMessage("위치 정보 제공이 필요한 서비스입니다.")
                .setDeniedMessage("[설정] -> [권한]에서 권한 변경이 가능합니다.")
                .setDeniedCloseButtonText("닫기")
                .setGotoSettingButtonText("설정")
                .setRationaleTitle("택시 리뷰 아자 위치 권한")
                .setPermissions(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                .check()
        }
    }

    // 네이버맵 불러오기가 완료되면 콜백
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        // 내장 위치 추적 기능 사용
        naverMap.locationSource = locationSource

        // 빨간색 표시 마커 (네이버맵 현재 가운데에 항상 위치)
        val marker = Marker()
        marker.position = LatLng(
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_location_on_24)
        marker.map = naverMap
        marker.iconTintColor = Color.RED

        // 카메라의 움직임에 대한 이벤트 리스너 인터페이스.
        // 참고 : https://navermaps.github.io/android-map-sdk/reference/com/naver/maps/map/package-summary.html
        naverMap.addOnCameraChangeListener { reason, animated ->
            Log.i("NaverMap", "카메라 변경 - reson: $reason, animated: $animated")
            marker.position = LatLng(
                // 현재 보이는 네이버맵의 정중앙 가운데로 마커 이동
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // 주소 텍스트 세팅 및 확인 버튼 비활성화
            binding.tvLocation.run {
                text = "위치 이동 중"
                setTextColor(Color.parseColor("#c4c4c4"))
            }
            binding.btnConfirm.run {
                setBackgroundResource(R.drawable.edittext_rounded_corner_rectangle)
                setTextColor(Color.parseColor("#ffffff"))
                isEnabled = false
            }
        }

        // 카메라의 움직임 종료에 대한 이벤트 리스너 인터페이스.
        naverMap.addOnCameraIdleListener {
            marker.position = LatLng(
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // 좌표 -> 주소 변환 텍스트 세팅, 버튼 활성화
            binding.tvLocation.run {
                text = getAddress(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
                setTextColor(Color.parseColor("#2d2d2d"))
            }
            binding.btnConfirm.run {
                setBackgroundResource(R.drawable.edittext_rounded_corner_rectangle)
                setTextColor(Color.parseColor("#FF000000"))
                isEnabled = true
            }
        }

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
        // 사용자 현재 위치 받아오기
        var currentLocation: Location?
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                currentLocation = location
                // 위치 오버레이의 가시성은 기본적으로 false로 지정되어 있습니다. 가시성을 true로 변경하면 지도에 위치 오버레이가 나타납니다.
                // 파랑색 점, 현재 위치 표시
                naverMap.locationOverlay.run {
                    isVisible = true
                    position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                }

                // 카메라 현재위치로 이동
                val cameraUpdate = CameraUpdate.scrollTo(
                    LatLng(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude
                    )
                )
                naverMap.moveCamera(cameraUpdate)

                // 빨간색 마커 현재위치로 변경
                marker.position = LatLng(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
            }
    }

    // 좌표 -> 주소 변환
    private fun getAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(requireContext(), Locale.KOREA)
        val address: ArrayList<Address>
        var addressResult = "주소를 가져 올 수 없습니다."
        try {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
            if (address.size > 0) {
                // 주소 받아오기
                val currentLocationAddress = address[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}