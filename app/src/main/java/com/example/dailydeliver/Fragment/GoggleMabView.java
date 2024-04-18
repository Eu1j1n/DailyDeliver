package com.example.dailydeliver.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.dailydeliver.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoggleMabView extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;

    String TAG = "구글맵뷰";

    private ImageButton googleBackBtn;
    private TextView addressTextView;
    private Button googleCompleteButton;
    private Marker currentMarker;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goggle_mab_view);

        googleBackBtn = findViewById(R.id.googlebtnBack);
        googleCompleteButton = findViewById(R.id.googleCompleteButton);
        addressTextView = findViewById(R.id.googleAddresstextView);

        googleCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullAddress = addressTextView.getText().toString();

                // 주소 정보를 시, 구, 동 단위로 분할하여 배열에 저장
                String[] addressParts = fullAddress.split(" ");

                String dong = "";
                double latitude = 0.0;
                double longitude = 0.0;

                // 시, 구, 동 데이터 추출
                if (addressParts.length >= 4) {
                    dong = addressParts[4]; // 동
                    Log.d(TAG, "dong" + dong);
                }

                // 현재 마커의 위치에서 위도와 경도 추출
                if (currentMarker != null) {
                    LatLng position = currentMarker.getPosition();
                    latitude = position.latitude;
                    longitude = position.longitude;
                }

                // 시, 구, 동, 위도, 경도 데이터를 Intent에 추가
                Intent resultIntent = new Intent();
                resultIntent.putExtra("DONG", dong);
                resultIntent.putExtra("LATITUDE", latitude);
                resultIntent.putExtra("LONGITUDE", longitude);
                resultIntent.putExtra("FULLADDRESS", fullAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });


        googleBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        requestLocationPermission();
        getLastKnownLocation(); // 마지막으로 알려진 위치를 가져옵니다.
    }

    private void requestLocationPermission() {
        // 위치 권한을 요청합니다.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용되었을 때
                // 위치 업데이트 요청을 시작합니다.
                startLocationUpdates();
            } else {
                // 위치 권한이 거부되었을 때
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (currentMarker != null) {
                    currentMarker.remove();
                }

                currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("현재 위치").draggable(true));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

        mMap.clear();
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("선택한 위치").draggable(true));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        getAddressFromLocation(latLng);
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                Log.d(TAG, "latitude" + latLng.latitude);
                Log.d(TAG, "longitude" + latLng.longitude);
                addressTextView.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "주소를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastKnownLocation() {
        // Check if the app has permission to access location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            requestLocationPermission();
            return;
        }

        // FusedLocationProviderClient 객체를 사용하여 마지막으로 알려진 위치를 가져옵니다.
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // 마지막으로 알려진 위치가 있을 경우 처리합니다.
                        handleLocation(location);
                    } else {
                        // 마지막으로 알려진 위치가 없는 경우 새로운 위치 업데이트를 요청합니다.
                        requestNewLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to get last known location
                    Toast.makeText(this, "Failed to get last known location", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "getLastKnownLocation: " + e.getMessage());
                });
    }

    private void requestNewLocation() {
        // 위치 업데이트 요청을 시작합니다.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한이 없으면 권한을 요청합니다.
            requestLocationPermission();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // 새로운 위치가 업데이트되면 처리합니다.
                handleLocation(location);

                // 위치 업데이트를 더 이상 필요하지 않을 경우 위치 업데이트를 중지합니다.
                locationManager.removeUpdates(this);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        });
    }

    private void handleLocation(Location location) {
        // 위치를 처리하는 코드를 여기에 작성합니다.
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
        currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("현재 위치").draggable(true));
    }
}
