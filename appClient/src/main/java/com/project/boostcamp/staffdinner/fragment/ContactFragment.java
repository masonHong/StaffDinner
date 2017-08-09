package com.project.boostcamp.staffdinner.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.project.boostcamp.publiclibrary.api.DataReceiver;
import com.project.boostcamp.publiclibrary.api.RetrofitClient;
import com.project.boostcamp.publiclibrary.data.DataEvent;
import com.project.boostcamp.publiclibrary.domain.ContactDTO;
import com.project.boostcamp.publiclibrary.util.SharedPreperenceHelper;
import com.project.boostcamp.staffdinner.R;
import com.project.boostcamp.staffdinner.activity.ContactDetailActivity;
import com.project.boostcamp.staffdinner.adapter.ContactRecyclerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hong Tae Joon on 2017-07-25.
 */

public class ContactFragment extends Fragment {
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.help_empty) View viewEmpty;
    private ContactRecyclerAdapter adapter;

    public static ContactFragment newInstance() {
        ContactFragment fragment = new ContactFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact, container, false);
        setupView(v);
        loadData();
        // TODO: 2017-07-31 로컬로 저장하고 맵은 사진으로 임시 저장하도록
        return v;
    }

    private void setupView(View v) {
        ButterKnife.bind(this, v);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactRecyclerAdapter(getContext(), dataEvent);
        recyclerView.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(onRefreshListener);
    }

    private void loadData() {
        String clientID = SharedPreperenceHelper.getInstance(getContext()).getLoginId();
        RetrofitClient.getInstance().getContacts(clientID, dataReceiver);
    }

    private DataEvent<ContactDTO> dataEvent = new DataEvent<ContactDTO>() {
        @Override
        public void onClick(ContactDTO data) {
            Intent intent = new Intent(getContext(), ContactDetailActivity.class);
            intent.putExtra(ContactDTO.class.getName(), data);
            startActivity(intent);
        }
    };

    private DataReceiver<ArrayList<ContactDTO>> dataReceiver = new DataReceiver<ArrayList<ContactDTO>>() {
        @Override
        public void onReceive(ArrayList<ContactDTO> data) {
            adapter.setData(data);
            if(data.size() == 0) {
                viewEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                viewEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            if(swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
        }

        @Override
        public void onFail() {
            Toast.makeText(getContext(), "서버 오류", Toast.LENGTH_SHORT).show();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
        }
    };
}
