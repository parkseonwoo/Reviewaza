package com.app.service.reviewaza.call

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.*
import com.app.service.reviewaza.databinding.ActivityCallBinding
import com.app.service.reviewaza.login.UserItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CallActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener{

    private val mapView : MapView by lazy {
        findViewById(R.id.mapView)
    }

    private lateinit var binding: ActivityCallBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    val arrowheadPath = ArrowheadPathOverlay()

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
    private var myUserId: String = ""
    private var myUserImage: String = ""
    private var myUserName: String = ""
    private var isInit = false

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        if(CALL_RESPONSE.equals("YES_RESPONSE")) {
            Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show()
            binding.taxiCallButton.isEnabled = true
            binding.taxiCallButton.isClickable = true
            binding.taxiCallButton.setText("?????? ??????!")
            CALL_RESPONSE = "RESPONSE"
            finish()
        }
        else if(CALL_RESPONSE.equals("NO_RESPONSE")) {
            Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show()
            binding.taxiCallButton.isEnabled = true
            binding.taxiCallButton.isClickable = true
            binding.taxiCallButton.setText("?????? ??????!")
            CALL_RESPONSE = "RESPONSE"
        }
        else {
            binding.taxiCallButton.setText("??????")
        }

    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        init()

        chatRoomId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId = Firebase.auth.currentUser?.uid ?: ""
        linearLayoutManager = LinearLayoutManager(applicationContext)

        Firebase.database.reference.child(Key.DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""
                myUserImage = myUserItem?.userImage ?: ""

                getOtherUserData()
            }

        mapView.onCreate(savedInstanceState)

        binding.taxiCallButton.setOnClickListener {

            val message = binding.callMessage.text.toString()

            if (!isInit) {
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "??? ???????????? ????????? ?????? ????????????", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId
            )

            Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "${Key.DB_CALLS}/$myUserId/$otherUserId/lastMessage" to message,
                "${Key.DB_CALLS}/$otherUserId/$myUserId/lastMessage" to message,
                "${Key.DB_CALLS}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${Key.DB_CALLS}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${Key.DB_CALLS}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            Firebase.database.reference.updateChildren(updates)

            val client = OkHttpClient()

            val root = JSONObject()
            val notification = JSONObject()
            val data = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)
            data.put("myUserId", myUserId)
            data.put("userName", myUserName)
            data.put("userImage", myUserImage)
            data.put("currentLocation", CURRENT_LOCATION)
            data.put("destination", DESTINATION)
            data.put("chatRoomId", chatRoomId)
            root.put("to", otherUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)
            root.put("data", data)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=${getString(R.string.FCM_SERVER_KEY)}").build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.stackTraceToString()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("ChatActivity", response.toString())
                }

            })

            binding.callMessage.text.clear()

            if(intent.getBooleanExtra("callResponse", false).equals("true")) {
                Log.e("noti test", "??????!")
                finish()
            }

            binding.taxiCallButton.apply {
                setText("?????????...")
                isEnabled = false
                isClickable = false

            }

        }

    }

    private fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // ?????? ?????????
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getOtherUserData() {
        Firebase.database.reference.child(Key.DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                Log.e("call otherFcmToken", "${otherUserFcmToken}")

                isInit = true
                getChatData()
            }
    }

    private fun getChatData() {
        Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatItemList.add(chatItem)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}

            })
    }

    companion object {
        const val EXTRA_CALL_ID = "CALL_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    // ???????????? ??????????????? ???????????? ??????
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        var currentPos: LatLng = LatLng(37.5670135, 126.9783740)

        // ?????? ?????? ?????? ?????? ??????
        naverMap.locationSource = locationSource

        // ????????? ?????? ?????? (???????????? ?????? ???????????? ?????? ??????)
        val marker = Marker()
        marker.position = LatLng(
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_location_on_24)
        marker.iconTintColor = Color.RED
        marker.map = naverMap

        // ???????????? ???????????? ?????? ????????? ????????? ???????????????.
        // ?????? : https://navermaps.github.io/android-map-sdk/reference/com/naver/maps/map/package-summary.html
        naverMap.addOnCameraChangeListener { reason, animated ->
            Log.i("NaverMap", "????????? ?????? - reson: $reason, animated: $animated")
            marker.position = LatLng(
                // ?????? ????????? ??????????????? ????????? ???????????? ?????? ??????
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            arrowheadPath.map = null
            // ?????? ????????? ?????? ??? ?????? ?????? ????????????
            binding.destinationValue.run {
                text = "?????? ?????? ???"
                setTextColor(Color.parseColor("#c4c4c4"))
            }
            binding.taxiCallButton.run {
                setBackgroundResource(R.drawable.edittext_rounded_corner_rectangle)
                setTextColor(Color.parseColor("#ffffff"))
                isEnabled = false
            }
        }

        // ???????????? ????????? ????????? ?????? ????????? ????????? ???????????????.
        naverMap.addOnCameraIdleListener {
            marker.position = LatLng(
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // ?????? -> ?????? ?????? ????????? ??????, ?????? ?????????
            binding.destinationValue.run {
                text = getAddress(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )

                DESTINATION = text.toString()
                setTextColor(Color.parseColor("#2d2d2d"))
            }

            binding.taxiCallButton.run {
                setBackgroundResource(R.drawable.edittext_rounded_corner_rectangle)
                setTextColor(Color.parseColor("#FF000000"))
                isEnabled = true

                getPath(currentPos, marker.position)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // ????????? ?????? ?????? ????????????
        var currentLocation: Location?
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                currentLocation = location

                // ?????? ??????????????? ???????????? ??????????????? false??? ???????????? ????????????. ???????????? true??? ???????????? ????????? ?????? ??????????????? ???????????????.
                // ????????? ???, ?????? ?????? ??????
                naverMap.locationOverlay.run {
                    isVisible = true
                    position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                    currentPos = position
                }
                val text = getAddress(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )
                binding.currentLocationValue.setText(text)
                CURRENT_LOCATION = text

                // ????????? ??????????????? ??????
                val cameraUpdate = CameraUpdate.scrollTo(
                    LatLng(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude
                    )
                )
                naverMap.moveCamera(cameraUpdate)


                // ????????? ?????? ??????????????? ??????
                marker.position = LatLng(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
            }
    }

    // ?????? -> ?????? ??????
    private fun getAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(this, Locale.KOREA)
        val address: ArrayList<Address>
        var addressResult = "????????? ?????? ??? ??? ????????????."
        try {
            //????????? ??????????????? ????????? ?????? ????????? ?????? ?????? ?????????
            //???????????? ?????? ??????????????? ????????? ????????????????????? ??????????????? ???????????? ?????? ???????????? ??????
            address = geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
            if (address.size > 0) {
                // ?????? ????????????
                val currentLocationAddress = address[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }

    // ???????????? ?????? ??????
    private fun requestPermissions() {
        // ?????? ?????? ?????? ?????? ??????
        locationSource =
            FusedLocationSource(this, LocationEnrollFragment.LOCATION_PERMISSION_REQUEST_CODE)

        // ??? ?????? ?????? ????????? ??????
        if (Build.VERSION.SDK_INT >= 23) {

            TedPermission.create()
                .setPermissionListener(object: PermissionListener {
                    override fun onPermissionGranted() {
                        Log.e("?????? ?????? ?????? ", "??????")
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        TODO("Not yet implemented")
                    }

                })
                .setRationaleMessage("?????? ?????? ????????? ????????? ??????????????????.")
                .setDeniedMessage("[??????] -> [??????]?????? ?????? ????????? ???????????????.")
                .setDeniedCloseButtonText("??????")
                .setGotoSettingButtonText("??????")
                .setRationaleTitle("HELLO")
                .setPermissions(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                .check()
        }
    }

    private fun getPath(curLatLng: LatLng, desLatLng: LatLng) {

        arrowheadPath.color = Color.YELLOW
        arrowheadPath.headSizeRatio = 2f
        arrowheadPath.width = 20

        arrowheadPath.coords = listOf(
            LatLng(curLatLng.latitude, curLatLng.longitude),
            LatLng(desLatLng.latitude, desLatLng.longitude)
        )
        arrowheadPath.map = naverMap
    }

    override fun onClick(p0: Overlay): Boolean {
        TODO("Not yet implemented")
    }


}