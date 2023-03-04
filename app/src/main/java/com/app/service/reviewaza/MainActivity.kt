package com.app.service.reviewaza

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.service.reviewaza.call.CallListFragment
import com.app.service.reviewaza.databinding.ActivityMainBinding
import com.app.service.reviewaza.mypage.MyPageFragment
import com.app.service.reviewaza.reviews.ReviewListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = LocationEnrollFragment()
    private val reviewListFragment = ReviewListFragment()
    private val callFragment = CallListFragment()
    private val myPageFragment = MyPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeButton -> {
                    replaceFragment(homeFragment)
                    supportActionBar?.title = "홈"
                    return@setOnItemSelectedListener true
                }
                R.id.reviewButton -> {
                    replaceFragment(reviewListFragment)
                    supportActionBar?.title = "리뷰"
                    return@setOnItemSelectedListener true
                }
                R.id.callButton -> {
                    replaceFragment(callFragment)
                    supportActionBar?.title = "호출"
                    return@setOnItemSelectedListener true
                }
                R.id.mypageButton -> {
                    replaceFragment(myPageFragment)
                    supportActionBar?.title = "마이페이지"
                    return@setOnItemSelectedListener true
                }
                else -> {
                    supportActionBar?.title = "친구"
                    return@setOnItemSelectedListener false
                }
            }
        }
        replaceFragment(homeFragment)

        askNotificationPermission()

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout, fragment)
            commit()
        }
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