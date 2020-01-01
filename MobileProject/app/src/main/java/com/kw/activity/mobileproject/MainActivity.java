package com.kw.activity.mobileproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String url = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=nSNwVbOGXdbUagAgA6bWWAgj2fqsc818oVdil2bcTuHjta6agG0cXFgtvpCHFx9mdFU343wxjPnAt5HhY7vSCw%3D%3D&numOfRows=10&pageNo=1&";
    private TextView txtLocation, txtDate, txtMain, txtSub, txtMisae, txtChomisae, txtYisanhwa, txtOzone;
    private TextView txtMisaeGrade, txtChoMisaeGrade, txtYisanhwaGrade, txtOzoneGrade;
    private ImageView imgMain, imgMisae, imgChomisae, imgYisanhwa, imgOzone;
    private int misaeGrade, chomisaeGrade, ozoneGrade, yisanhwaGrade;

    private String[] strSub = {"대기 상태가 매우 좋아요", "대기 상태가 보통이에요", "대기 상태가 나빠요", "대기 상태가 매우 나빠요"};

    /* VERSION 2 - Add Location Service */
    /*
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
     */
    private String changeUrl = "http://convertcoord.appspot.com/geom/convert.hj?";
    private String stationUrl = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?serviceKey=nSNwVbOGXdbUagAgA6bWWAgj2fqsc818oVdil2bcTuHjta6agG0cXFgtvpCHFx9mdFU343wxjPnAt5HhY7vSCw%3D%3D&";

    private String beforeX = null;
    private String beforeY = null;
    private String afterX = null;
    private String afterY = null;
    private String stationName = null;

    APITask apiTask = null;

    /* VERSION 3 */
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLocation = findViewById(R.id.txtLocation);
        txtDate = findViewById(R.id.txtDate);
        txtMain = findViewById(R.id.txtMain);
        txtSub = findViewById(R.id.txtSub);
        txtMisae = findViewById(R.id.txtMisae);
        txtChomisae = findViewById(R.id.txtChomisae);
        txtYisanhwa = findViewById(R.id.txtYisanhwa);
        txtOzone = findViewById(R.id.txtOzone);
        txtMisaeGrade = findViewById(R.id.txtMisaeGrade);
        txtChoMisaeGrade = findViewById(R.id.txtChomisaeGrade);
        txtYisanhwaGrade = findViewById(R.id.txtYisanhwaGrade);
        txtOzoneGrade = findViewById(R.id.txtOzoneGrade);

        imgMain = findViewById(R.id.imgMain);
        imgMisae = findViewById(R.id.imgMisae);
        imgChomisae = findViewById(R.id.imgChomisae);
        imgYisanhwa = findViewById(R.id.imgYisanhwa);
        imgOzone = findViewById(R.id.imgOzone);

        apiTask = new APITask();

        // add internet permission
        if(checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        gpsTracker = new GpsTracker(MainActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        address = getCurrentAddress(latitude, longitude);
        address = address.substring(5);
        address = address.replace("\n", "");
        Log.d("hongchan", address);
        txtLocation.setText(address);
        //Toast.makeText(MainActivity.this, address, Toast.LENGTH_LONG).show();
        //Toast.makeText(MainActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

        beforeY = "" + latitude;
        beforeX = "" + longitude;

        apiTask.execute(url);
        /*
        // get user location (func)
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // find location - call callback func
        mGoogleApiClient.connect();
         */
    }

    public class APITask extends AsyncTask<String, String , Boolean> {
        @Override
        protected void onPreExecute() {
            changeUrl += "x=" + beforeX + "&y=" + beforeY + "&inCoord=Wgs84&outCoord=TmMid";

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try{
                /* find tmx, tmy */
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document locDoc = builder.parse(changeUrl);

                locDoc.getDocumentElement().normalize();
                NodeList locNodeList = locDoc.getElementsByTagName("output");
                Node locNode = locNodeList.item(0);
                Element locElement = (Element)locNode;

                afterX = getTagValue("x", locElement);
                afterY = getTagValue("y", locElement);
                stationUrl += "tmX=" + afterX + "&tmY=" + afterY;
                Log.d("hongchan", "Changed Position : " + afterX + ", " + afterY);

                /* find station name */
                Document stationDoc = builder.parse(stationUrl);
                stationDoc.getDocumentElement().normalize();
                NodeList stationNodeList = stationDoc.getElementsByTagName("item");
                Node stationNode = stationNodeList.item(0);
                Element stationElement = (Element)stationNode;

                stationName = getTagValue("stationName", stationElement);
                url += "stationName=" + stationName + "&dataTerm=DAILY&ver=1.3";
                publishProgress("STATION", stationName);

                Log.d("hongchan", stationName);
            }
            catch(Exception e){
                Log.d("hongchan", "[ERROR1] " + e.getMessage());
                e.printStackTrace();
            }

            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(url);

                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getElementsByTagName("item");
                Node node = nodeList.item(0);
                Element element = (Element)node;

                publishProgress("DATE", getTagValue("dataTime", element));

                publishProgress("PM10", getTagValue("pm10Value", element));
                publishProgress("PM25", getTagValue("pm25Value", element));
                publishProgress("YISANHWA", getTagValue("no2Value", element));
                publishProgress("OZONE", getTagValue("o3Value", element));

                publishProgress("PM10_GRADE", getTagValue("pm10Grade1h", element));
                publishProgress("PM25_GRADE", getTagValue("pm25Grade1h", element));
                publishProgress("YISANHWA_GRADE", getTagValue("no2Grade", element));
                publishProgress("OZONE_GRADE", getTagValue("o3Grade", element));

                publishProgress("UPDATE", "");
            }
            catch(Exception e){
                Log.d("hongchan", "[ERROR2] " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String tag = values[0];
            String value = values[1];

            //if(tag.equals("STATION")){
            //    txtLocation.setText(address);
            //}
            if(tag.equals("DATE")){
                txtDate.setText(value);
            }

            else if(tag.equals("PM10")){
                txtMisae.setText(value + " μg/m³");
            }
            else if(tag.equals("PM25")){
                txtChomisae.setText(value + " μg/m³");
            }
            else if(tag.equals("YISANHWA")){
                txtYisanhwa.setText(value + " ppm");
            }
            else if(tag.equals("OZONE")){
                txtOzone.setText(value + " ppm");
            }

            else if(tag.equals("PM10_GRADE")){
                try{
                    misaeGrade = Integer.parseInt(value);
                }
                catch(Exception e){
                    misaeGrade = 0;
                }
            }
            else if(tag.equals("PM25_GRADE")){
                try{
                    chomisaeGrade = Integer.parseInt(value);
                }
                catch(Exception e){
                    chomisaeGrade = 0;
                }
            }
            else if(tag.equals("YISANHWA_GRADE")){
                try{
                    yisanhwaGrade = Integer.parseInt(value);
                }
                catch(Exception e){
                    yisanhwaGrade = 0;
                }
            }
            else if(tag.equals("OZONE_GRADE")){
                try{
                    ozoneGrade = Integer.parseInt(value);
                }
                catch(Exception e){
                    ozoneGrade = 0;
                }
            }

            else if(tag.equals("UPDATE")){
                // update each txt
                txtMisaeGrade.setText(getGrade(misaeGrade));
                txtChoMisaeGrade.setText(getGrade(chomisaeGrade));
                txtYisanhwaGrade.setText(getGrade(yisanhwaGrade));
                txtOzoneGrade.setText(getGrade(ozoneGrade));

                // update each img
                imgMisae.setImageResource(getImgSrc(misaeGrade));
                imgChomisae.setImageResource(getImgSrc(chomisaeGrade));
                imgYisanhwa.setImageResource(getImgSrc(yisanhwaGrade));
                imgOzone.setImageResource(getImgSrc(ozoneGrade));

                if(misaeGrade == 4 || chomisaeGrade == 4 || yisanhwaGrade == 4 || ozoneGrade == 4){
                    // VERY BAD
                    imgMain.setImageResource(R.drawable.verybad);
                    txtMain.setText("매우 나쁨");
                    txtSub.setText(strSub[3]);
                }
                else if(misaeGrade == 3 || chomisaeGrade == 3 || yisanhwaGrade == 3 || ozoneGrade == 3){
                    // BAD
                    imgMain.setImageResource(R.drawable.bad);
                    txtMain.setText("나쁨");
                    txtSub.setText(strSub[2]);
                }
                else if(misaeGrade == 2 || chomisaeGrade == 2 || yisanhwaGrade == 2 || ozoneGrade == 2){
                    // NORMAL
                    imgMain.setImageResource(R.drawable.normal);
                    txtMain.setText("보통");
                    txtSub.setText(strSub[1]);
                }
                else{
                    // GOOD
                    imgMain.setImageResource(R.drawable.good);
                    txtMain.setText("좋음");
                    txtSub.setText(strSub[0]);
                }
            }

            super.onProgressUpdate(values);
        }

        private String getGrade(int num){
            switch(num){
                case 1:
                    return "좋음";
                case 2:
                    return "보통";
                case 3:
                    return "나쁨";
                case 4:
                    return "매우 나쁨";
                default:
                    return "정보 없음";
            }
        }

        private int getImgSrc(int num){
            switch(num){
                case 1:
                    return R.drawable.good;
                case 2:
                    return R.drawable.normal;
                case 3:
                    return R.drawable.bad;
                case 4:
                    return R.drawable.verybad;
                default:
                    return R.drawable.noinfo;
            }
        }
    }

    public static String getTagValue(String tag, Element element){
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();

        Node value = (Node)nodeList.item(0);
        if(value == null){
            return null;
        }
        return value.getNodeValue();
    }

    public void onClick(View v){
        Intent intent = new Intent(this, ForcastActivity.class);
        intent.putExtra("DATE", txtDate.getText().toString());
        startActivity(intent);
    }


    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

            }
            else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}

