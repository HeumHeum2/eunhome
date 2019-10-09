package com.example.eunhome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class HomeFragment extends Fragment {

    private static final String TAG= "HomeFragment";

    private TextView textWelcome, textDeviceCheck;
    private FloatingActionButton fabDeviceAdd;
    private RecyclerView userDeviceRecyclerView;
    private SharedPreferences userinfo;
    private ArrayList<ListUsersQuery.Item> mUsers;
    private DeviceAdapter adapter;
    private ProgressBar deviceLodingBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userinfo = getContext().getSharedPreferences("userinfo",Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //액션바 세팅
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.hide();
        init(view);

        return view;
    }

    public void init(View view){ // 레이아웃 세팅
        textWelcome = view.findViewById(R.id.textWelcome);
        textDeviceCheck = view.findViewById(R.id.textDeviceCheck);
        fabDeviceAdd = view.findViewById(R.id.fabDeviceAdd);
        deviceLodingBar = view.findViewById(R.id.deviceLoadingbar);

        userDeviceRecyclerView = view.findViewById(R.id.userDeviceRecyclerView);
        userDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeviceAdapter(getContext());
        userDeviceRecyclerView.setAdapter(adapter);
//        SwipeController swipeController = new SwipeController();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback); // 나중에 swipeController로 바꿔줘야함.(스와이프로 삭제)
        itemTouchHelper.attachToRecyclerView(userDeviceRecyclerView);

        textWelcome.setText(getString(R.string.welcome, userinfo.getString("name",null)));

        click();
        ClientFactory.init(getContext());
    }

    private void click() {
        fabDeviceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),DeviceAddActivity.class);
                startActivity(intent);
            }
        });
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            mUsers.remove(position);
            adapter.notifyItemRemoved(position);
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        query();
    }

    public void query(){
        deviceLodingBar.setVisibility(View.VISIBLE);
        ClientFactory.appSyncClient().query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK) //캐시를 가져옴
                .enqueue(usersCallback);
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> usersCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            mUsers = new ArrayList<>(response.data().listUsers().items());
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    adapter.setItems(mUsers);
                    adapter.notifyDataSetChanged();
                    if(mUsers.size() != 0){
                        textDeviceCheck.setVisibility(View.GONE);
                        deviceLodingBar.setVisibility(View.GONE);
                    }else{
                        textDeviceCheck.setVisibility(View.VISIBLE);
                        deviceLodingBar.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };
}
