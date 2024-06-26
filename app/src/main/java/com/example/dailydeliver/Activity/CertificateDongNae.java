package com.example.dailydeliver.Activity;

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

public class CertificateDongNae extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;

    String TAG = "동네인증";

    private ImageButton certificateBtnBack;
    private TextView addressTextView;
    private Button certificateDongNaeButton;
    private Marker currentMarker;

    private LatLng currentLatLng;
    private LatLng destinationLatLng;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final double DISTANCE_THRESHOLD_METERS = 2000.0; // 2km

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_dong_nae);

        certificateBtnBack = findViewById(R.id.certificateBtnBack);
        certificateDongNaeButton = findViewById(R.id.certificateDongNaeButton);

        Intent intent = getIntent();
        String address = intent.getStringExtra("address");

        destinationLatLng = getLocationFromAddress(this, address); // 사용자가 입력한 주소값
        Log.d(TAG, "Destination LatLng: " + destinationLatLng);

        certificateDongNaeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLatLng != null && destinationLatLng != null) {
                    double distance = distanceBetween(currentLatLng, destinationLatLng);
                    if (distance <= DISTANCE_THRESHOLD_METERS) {
                        // 동네 인증 성공
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        // 동네 인증 실패
                        Toast.makeText(CertificateDongNae.this, "동네 인증에 실패했습니다." , Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CertificateDongNae.this, "현재 위치를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        certificateBtnBack.setOnClickListener(new View.OnClickListener() {
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
        getLastKnownLocation(); // 마지막으로 알려진 위치를 가져오기
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
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (currentMarker != null) {
                    currentMarker.remove();
                }

                currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("현재 위치").draggable(true));
                Log.d(TAG, "onLocationChanged: 처음에 마커 여기임?" + currentMarker);

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
        // Map 클릭 리스너 제거
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                Log.d(TAG, "latitude" + latLng.latitude);
                Log.d(TAG, "longitude" + latLng.longitude);
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

        // FusedLocationProviderClient 객체를 사용하여 마지막으로 알려진 위치를 가져오기
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        handleLocation(location);
                    } else {
                        // 마지막으로 알려진 위치가 없는 경우 새로운 위치 업데이트를 요청
                        requestNewLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "getLastKnownLocation: " + e.getMessage());
                });
    }

    private LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        LatLng latLng = null;

        try {
            addresses = geocoder.getFromLocationName(strAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                latLng = new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return latLng;
    }

    private void requestNewLocation() {
        // 위치 업데이트 요청을 시작
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한이 없으면 권한을 요청
            requestLocationPermission();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // 새로운 위치가 업데이트되면 처리
                handleLocation(location);

                // 위치 업데이트를 더 이상 필요하지 않을 경우 위치 업데이트를 중지
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

    private double distanceBetween(LatLng latLng1, LatLng latLng2) {
        Location location1 = new Location("");
        location1.setLatitude(latLng1.latitude);
        location1.setLongitude(latLng1.longitude);

        Location location2 = new Location("");
        location2.setLatitude(latLng2.latitude);
        location2.setLongitude(latLng2.longitude);

        return location1.distanceTo(location2);
    }

    private void handleLocation(Location location) {
        // 위치를 처리하는 코드를 여기에 작성
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 위도와 경도
        Log.d(TAG, "위경도: " + currentLatLng.latitude + ", Longitude: " + currentLatLng.longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
        currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("현재 위치").draggable(true));
    }
}
