package com.app.service.reviewaza.call

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.BaseFragment
import com.app.service.reviewaza.R
import com.app.service.reviewaza.call.Key.Companion.DB_USERS
import com.app.service.reviewaza.databinding.FragmentCalllistBinding
import com.app.service.reviewaza.databinding.FragmentReviewsBinding
import com.app.service.reviewaza.login.UserItem
import com.app.service.reviewaza.reviews.ReviewListAdapter
import com.app.service.reviewaza.reviews.ReviewListViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallListFragment : BaseFragment<FragmentCalllistBinding>(R.layout.fragment_calllist) {

    private lateinit var callListAdapter: CallListAdapter

    private val callListViewModel by lazy { ViewModelProvider(this).get(CallListViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        callListAdapter = CallListAdapter { otherUser ->

            val callId = callListViewModel.getchatRoomDB(otherUser)

            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(CallActivity.EXTRA_CALL_ID, callId)
            intent.putExtra(CallActivity.EXTRA_OTHER_USER_ID, otherUser.userId)
            startActivity(intent)
        }

        binding.toolBar.apply {
            title = "호출 목록"
        }

        binding.callListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = callListAdapter
        }

        observerData()
    }

    fun observerData() {
        callListViewModel.fetchData().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            callListAdapter.submitList(it)
        })

    }

    override fun init() {}
}