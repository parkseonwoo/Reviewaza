package com.app.service.reviewaza

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.app.service.reviewaza.call.CallListFragment
import com.app.service.reviewaza.databinding.ActivityMainBinding
import com.app.service.reviewaza.mypage.MyPageFragment
import com.app.service.reviewaza.reviews.ReviewListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val fragmentManager = supportFragmentManager

    private var homeFragment: LocationEnrollFragment? = null
    private var reviewListFragment : ReviewListFragment? = null
    private var callFragment : CallListFragment? = null
    private var myPageFragment : MyPageFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        initBottomNavigation()
    }

    private fun initBottomNavigation() {

        // 최초로 보이는 프래그먼트
        homeFragment = LocationEnrollFragment()
        fragmentManager.beginTransaction().replace(R.id.frameLayout, homeFragment!!).commit()

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeButton -> {
                    if(homeFragment == null) {
                        homeFragment = LocationEnrollFragment()
                        fragmentManager.beginTransaction().add(R.id.frameLayout, homeFragment!!).commit()
                    }
                    if(homeFragment != null) fragmentManager.beginTransaction().show(homeFragment!!).commit()
                    if(reviewListFragment != null) fragmentManager.beginTransaction().hide(reviewListFragment!!).commit()
                    if(callFragment != null) fragmentManager.beginTransaction().hide(callFragment!!).commit()
                    if(myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment!!).commit()
                    supportActionBar?.title = "홈"
                    return@setOnItemSelectedListener true
                }
                R.id.reviewButton -> {
                    if(reviewListFragment == null) {
                        reviewListFragment = ReviewListFragment()
                        fragmentManager.beginTransaction().add(R.id.frameLayout, reviewListFragment!!).commit()
                    }
                    if(homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment!!).commit()
                    if(reviewListFragment != null) fragmentManager.beginTransaction().show(reviewListFragment!!).commit()
                    if(callFragment != null) fragmentManager.beginTransaction().hide(callFragment!!).commit()
                    if(myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment!!).commit()
                    supportActionBar?.title = "리뷰"
                    return@setOnItemSelectedListener true
                }
                R.id.callButton -> {
                    if(callFragment == null) {
                        callFragment = CallListFragment()
                        fragmentManager.beginTransaction().add(R.id.frameLayout, callFragment!!).commit()
                    }
                    if(homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment!!).commit()
                    if(reviewListFragment != null) fragmentManager.beginTransaction().hide(reviewListFragment!!).commit()
                    if(callFragment != null) fragmentManager.beginTransaction().show(callFragment!!).commit()
                    if(myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment!!).commit()
                    supportActionBar?.title = "호출"
                    return@setOnItemSelectedListener true
                }
                R.id.mypageButton -> {
                    if(myPageFragment == null) {
                        myPageFragment = MyPageFragment()
                        fragmentManager.beginTransaction().add(R.id.frameLayout, myPageFragment!!).commit()
                    }
                    if(homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment!!).commit()
                    if(reviewListFragment != null) fragmentManager.beginTransaction().hide(reviewListFragment!!).commit()
                    if(callFragment != null) fragmentManager.beginTransaction().hide(callFragment!!).commit()
                    if(myPageFragment != null) fragmentManager.beginTransaction().show(myPageFragment!!).commit()
                    supportActionBar?.title = "마이페이지"
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }

        askNotificationPermission()

    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // 알림권한 없음
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionDialog()  // 한번 거절을 했고 두번째로 물어보는 경우 교육용 팝업을 띄어줘야함
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setMessage("알림 권한이 없으면 알림을 받을 수 없습니더")
            .setPositiveButton("권한 허용하기") { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton("취소") { dialogInterfaces, _ -> dialogInterfaces.cancel() }
    }

}