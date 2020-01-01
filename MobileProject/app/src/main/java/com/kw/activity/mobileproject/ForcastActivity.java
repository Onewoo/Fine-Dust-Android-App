package com.kw.activity.mobileproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ForcastActivity extends AppCompatActivity {
    /* VERSION 3 - Add Forcast Service */
    private String url = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMinuDustFrcstDspth?serviceKey=nSNwVbOGXdbUagAgA6bWWAgj2fqsc818oVdil2bcTuHjta6agG0cXFgtvpCHFx9mdFU343wxjPnAt5HhY7vSCw%3D%3D&numOfRows=10&pageNo=1&";
    private TextView txtForcastDate, txtForcastDateToday, txtForcastToday, txtForcastDateTomorrow, txtForcastTomorrow;
    private String date = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forcast);

        txtForcastDate = findViewById(R.id.txtForcastDate);
        txtForcastDateToday = findViewById(R.id.txtForcastDateToday);
        txtForcastToday = findViewById(R.id.txtForcastToday);
        txtForcastDateTomorrow = findViewById(R.id.txtForcastDateTomorrow);
        txtForcastTomorrow = findViewById(R.id.txtForcastTomorrow);

        Toast.makeText(this, "광운대학교 소프트웨어학부 윤홍찬", Toast.LENGTH_SHORT).show();
        Intent intent = getIntent();

        // Get Current Time
        String date = intent.getStringExtra("DATE");
        date = date.substring(0, 10);
        Log.d("hongchan", date);

        url += "searchDate=" + date + "&InformCode=PM10";

        ForcastAsyncTask forcastAsyncTask = new ForcastAsyncTask();
        forcastAsyncTask.execute(url);
    }

    private class ForcastAsyncTask extends AsyncTask<String, String, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(url);

                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getElementsByTagName("item");

                // Today
                Node nodeToday = nodeList.item(0);
                Element elementToday = (Element)nodeToday;

                publishProgress("DATE", MainActivity.getTagValue("dataTime", elementToday));

                publishProgress("INFO_TODAY", MainActivity.getTagValue("informOverall", elementToday));
                publishProgress("DATE_TODAY", MainActivity.getTagValue("informData", elementToday));


                // Tomorrow
                Node nodeTomorrow = nodeList.item(1);
                Element elementTomorrow = (Element)nodeTomorrow;

                publishProgress("INFO_TOMO", MainActivity.getTagValue("informOverall", elementTomorrow));
                publishProgress("DATE_TOMO", MainActivity.getTagValue("informData", elementTomorrow));
            }
            catch(Exception e){
                Log.d("hongchan", "[Forcast Error] " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String tag = values[0];
            String value = values[1];

            if(tag.equals("DATE")){
                txtForcastDate.setText(value + "\n(매일 5시, 11시, 17시, 23시 업데이트)");
            }

            else if(tag.equals("INFO_TODAY")){
                txtForcastToday.setText(value);
            }
            else if(tag.equals("DATE_TODAY")){
                txtForcastDateToday.setText("오늘 (" + value + ")");
            }

            else if(tag.equals("INFO_TOMO")){
                txtForcastTomorrow.setText(value);
            }
            else if(tag.equals("DATE_TOMO")){
                txtForcastDateTomorrow.setText("내일 (" + value + ")");
            }

            super.onProgressUpdate(values);
        }
    }
}
