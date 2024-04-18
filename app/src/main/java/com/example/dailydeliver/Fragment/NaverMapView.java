package com.example.dailydeliver.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.content.Intent;

import com.example.dailydeliver.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NaverMapView extends AppCompatActivity {

    private MapView mapView;

    String TAG = "네이버 맵뷰";
    private Button completeButton;
    private ImageButton mapBackButton;
    private TextView addressTextView;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private static final int REQUEST_CODE_ADDRESS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naver_map_view);

        mapView = findViewById(R.id.mapView);
        completeButton = findViewById(R.id.addressCompleteButton);
        addressTextView = findViewById(R.id.addressTextView);
        mapBackButton = findViewById(R.id.mapBackButton);

        mapBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TextView에서 주소 정보 가져오기
                String fullAddress = addressTextView.getText().toString();

                // 주소 정보를 시, 구, 동 단위로 분할하여 배열에 저장
                String[] addressParts = fullAddress.split(" ");

                String city = "";
                String gu = "";
                String dong = "";

                // 시, 구, 동 데이터 추출
                if (addressParts.length >= 2) {
                    city = addressParts[2]; // 시
                    Log.d(TAG, "city" + city);
                }
                if (addressParts.length >= 2) {
                    gu = addressParts[3]; // 구
                    Log.d(TAG, "gu" + gu);
                }
                if (addressParts.length >= 4) {
                    dong = addressParts[4]; // 동
                    Log.d(TAG, "dong" + dong);
                }

                // 시, 구, 동 데이터를 Intent에 추가
                Intent resultIntent = new Intent();
                resultIntent.putExtra("CITY", city);
                resultIntent.putExtra("GU", gu);
                resultIntent.putExtra("DONG", dong);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });


        geocoder = new Geocoder(this, Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (hasPermissions()) {
            // 권한 Ok면 맵 보여줌

           initializeMap();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    // 권한
    private boolean hasPermissions() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }



    private void initializeMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull NaverMap naverMap) {
                naverMap.setLocationSource(naverMap.getLocationSource());
                naverMap.getUiSettings().setLocationButtonEnabled(true);
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

                if (ActivityCompat.checkSelfPermission(NaverMapView.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(NaverMapView.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                // 위치 업데이트 콜백 등록
                naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
                    @Override
                    public void onLocationChange(@NonNull Location location) {
                        // 위치가 변경될 때마다 오버레이 업데이트
                        updateOverlay(naverMap, location);

                    }
                });

                // 초기 위치 설정
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(NaverMapView.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // 초기 오버레이 표시
                                    updateOverlay(naverMap, location);

                                    // 초기 위치로 카메라 이동
                                    naverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(location.getLatitude(), location.getLongitude())));
                                } else {
                                    Toast.makeText(NaverMapView.this, "Unable to get current location.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(NaverMapView.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 위치 가져오기 실패
                                Toast.makeText(NaverMapView.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                // 기본 카메라 위치 설정
                CameraPosition cameraPosition = new CameraPosition(
                        new LatLng(37.5666103, 126.9783882),  // Latitude, Longitude (Seoul City Hall)
                        16  // Zoom level
                );
                naverMap.setCameraPosition(cameraPosition);
            }
        });
    }


    // 오버레이 업데이트 메서드
    private void updateOverlay(@NonNull NaverMap naverMap, @NonNull Location location) {
        com.naver.maps.map.overlay.LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        if (locationOverlay == null) {
            locationOverlay = naverMap.getLocationOverlay();
        }
        locationOverlay.setVisible(true);
        locationOverlay.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

        // 주소 업데이트
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                addressTextView.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(NaverMapView.this, "Error occurred while getting the address.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasPermissions()) {
                initializeMap();
            } else {
                Toast.makeText(this, "퍼미션 없음.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Lifecycle methods for MapView
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
