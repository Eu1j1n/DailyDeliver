package com.example.dailydeliver.Fragment;

import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.dailydeliver.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;

    String TAG = "구글맵뷰";

    private ImageButton showLocationBackBtn;

    // 위도와 경도를 저장할 변수
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);

        // 인텐트로부터 위도와 경도 데이터를 받아옴
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        showLocationBackBtn = findViewById(R.id.showLocationExitButton);

        showLocationBackBtn.setOnClickListener(v -> finish());

        // 맵 프래그먼트를 가져와서 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 받아온 위도와 경도로 LatLng 객체 생성
        LatLng location = new LatLng(latitude, longitude);

        // 맵에 마커 추가
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title("거래 위치")
                .snippet("이 곳에서 심부름이 이루어집니다.");
        mMap.addMarker(markerOptions);

        // 해당 위치로 카메라 이동 및 줌
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
    }

}
