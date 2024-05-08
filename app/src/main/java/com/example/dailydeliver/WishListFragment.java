package com.example.dailydeliver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class WishListFragment extends Fragment {

    private static final String ARG_RECEIVED_ID = "receivedID";
    private String receivedID;

    public WishListFragment() {
        // Required empty public constructor
    }

    public static WishListFragment newInstance(String receivedID) {
        WishListFragment fragment = new WishListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECEIVED_ID, receivedID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receivedID = getArguments().getString(ARG_RECEIVED_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wish_list, container, false);
    }
}
