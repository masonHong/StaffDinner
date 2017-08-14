package com.project.boostcamp.staffdinnerrestraurant.fragment;

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
import com.project.boostcamp.publiclibrary.api.RetrofitAdmin;
import com.project.boostcamp.publiclibrary.inter.DataEvent;
import com.project.boostcamp.publiclibrary.domain.ContactDTO;
import com.project.boostcamp.publiclibrary.util.SQLiteHelper;
import com.project.boostcamp.publiclibrary.util.SharedPreperenceHelper;
import com.project.boostcamp.staffdinnerrestraurant.R;
import com.project.boostcamp.staffdinnerrestraurant.activity.ContactDetailActivity;
import com.project.boostcamp.staffdinnerrestraurant.adapter.ContactRecyclerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hong Tae Joon on 2017-07-28.
 */

public class ContactFragment extends Fragment {
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.help_empty) View viewEmpty;
    private ContactRecyclerAdapter adapter;

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_layout, container, false);
        setupView(v);
        loadData();
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

    public void loadData() {
        showRefreshing();
        String adminID = SharedPreperenceHelper.getInstance(getContext()).getLoginId();
        RetrofitAdmin.getInstance().getContacts(adminID, dataReceiver);
    }

    private DataEvent<ContactDTO> dataEvent = new DataEvent<ContactDTO>() {
        @Override
        public void onClick(ContactDTO data) {
            Intent intent = new Intent(getContext(), ContactDetailActivity.class);
            intent.putExtra(ContactDTO.class.getName(), data);
            startActivity(intent);
        }
    };

    /**
     * 계약서 목록을 불러왔을 때 결과 처리
     * - 성공할 경우
     * 서버에서 불러온 데이터를 보여주도록 처리한다.
     * 로컬에 저장된 계약 내역을 최신화 한다
     * - 실패할 경우
     * 로컬에 저장된 데이터를 보여주도록 처리한다.
     */
    private DataReceiver<ArrayList<ContactDTO>> dataReceiver = new DataReceiver<ArrayList<ContactDTO>>() {
        @Override
        public void onReceive(ArrayList<ContactDTO> data) {
            if(data == null) {
                data = new ArrayList<>();
            }
            if(data.size() == 0) {
                viewEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                viewEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                SQLiteHelper.getInstance(getContext()).refreshContact(data);
            }
            adapter.setData(data);
            if(swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
            hideRefreshing();
        }

        @Override
        public void onFail() {
            hideRefreshing();
            adapter.setData(SQLiteHelper.getInstance(getContext()).selectContact());
            Toast.makeText(getContext(), R.string.fail_to_load_contacts, Toast.LENGTH_SHORT).show();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
        }
    };

    private void showRefreshing() {
        if(!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }
    }

    private void hideRefreshing() {
        if(swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }
}
