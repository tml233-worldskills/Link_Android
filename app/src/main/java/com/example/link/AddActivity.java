package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class AddActivity extends AppCompatActivity {
    private static final int TAKE_PHOTO=1;
    private static final int CHOOSE_PHOTO=2;

    EditText editSiteName, editDate, editUrl, editBeizhu;
    Spinner spinnerGrade;
    TextView btnSubmit, btnCancel;
    ImageView imgDate;
    ListView listPhotos;
    Button btnCapture, btnBrowse;

    List<String> gradeNames =new ArrayList<>();
    List<Integer> gradeIds =new ArrayList<>();

    List<Bitmap> linkPhotos=new ArrayList<>();
    LinkPhotoAdapter adapter;

    int gradeId =-1;

    int editingId =-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        editSiteName = findViewById(R.id.add_edit_siteName);
        editUrl = findViewById(R.id.add_edit_url);
        spinnerGrade = findViewById(R.id.add_spinner_grade);
        editBeizhu = findViewById(R.id.add_edit_beizhu);

        imgDate = findViewById(R.id.add_img_date);
        editDate = findViewById(R.id.add_edit_date);

        btnCapture = findViewById(R.id.add_btn_capture);
        btnBrowse = findViewById(R.id.add_btn_browse);
        listPhotos = findViewById(R.id.add_list_photos);

        btnSubmit = findViewById(R.id.add_btn_submit);
        btnCancel = findViewById(R.id.add_btn_cancel);

        adapter=new LinkPhotoAdapter(this,linkPhotos);
        listPhotos.setAdapter(adapter);

        Intent intent=getIntent();
        editingId=intent.getIntExtra("editingId",-1);

        Future<String> gradesJsonFuture=Utils.threadPool.submit(new Callable<String>() {
            @Override
            public String call() {
                return Utils.sendGet(Utils.baseURL+"/grades");
            }
        });
        Future<String> linkJsonFuture=null;
        if(editingId>=0){
            linkJsonFuture=Utils.threadPool.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return Utils.sendGet(Utils.baseURL + "/links/" + editingId);
                }
            });
        }

        try {
            {
                gradeIds.clear();
                gradeNames.clear();
                gradeIds.add(-1);
                gradeNames.add("Please choose...");
                String gradesJson = gradesJsonFuture.get();
                JSONObject gradesObj = new JSONObject(gradesJson);
                JSONArray gradesArr = gradesObj.getJSONArray("data");
                for (int i = 0; i < gradesArr.length(); i++) {
                    JSONObject gradeObj = gradesArr.getJSONObject(i);
                    int ID = gradeObj.getInt("Id");
                    String Name = gradeObj.getString("Grade").trim();
                    gradeIds.add(ID);
                    gradeNames.add(Name);
                }
                spinnerGrade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gradeNames));
            }

            if(editingId>=0) {
                String linkJson=linkJsonFuture.get();
                JSONObject linkObj=new JSONObject(linkJson).getJSONObject("data");
                editSiteName.setText(linkObj.getString("siteName"));
                editUrl.setText(linkObj.getString("url"));
                spinnerGrade.setSelection(gradeIds.indexOf(linkObj.getInt("gradeId")));
                editBeizhu.setText(linkObj.getString("beizhu"));
                editDate.setText(linkObj.getString("time"));

                JSONArray photos=linkObj.getJSONArray("photos");
                for (int i=0;i<photos.length();i+=1){
                    String photo=photos.getString(i);
                    Bitmap bitmap=Utils.strToBitmap(photo);
                    linkPhotos.add(bitmap);
                }
                listPhotos.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                gradeId = gradeIds.get(i);
                adapterView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final Calendar calendar = Calendar.getInstance();
        imgDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        calendar.set(i, i1, i2);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        editDate.setText(sdf.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editSiteName.getText().toString().trim().equals("")) {
                    Toast.makeText(AddActivity.this, "Please input SiteName", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editUrl.getText().toString().trim().equals("")) {
                    Toast.makeText(AddActivity.this, "Please input Url", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (gradeId == -1) {
                    Toast.makeText(AddActivity.this, "Please select Grade", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editBeizhu.getText().toString().trim().equals("")) {
                    Toast.makeText(AddActivity.this, "Please input Beizhu", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("siteName", editSiteName.getText().toString().trim());
                    jsonObject.put("url", editUrl.getText().toString().trim());
                    jsonObject.put("gradeId", gradeId);
                    jsonObject.put("beizhu", editBeizhu.getText().toString().trim());
                    jsonObject.put("exp", editDate.getText().toString().trim());

                    JSONArray photosArr = new JSONArray();
                    for(Bitmap photo:linkPhotos) {
                        photosArr.put(Utils.bitmapToStr(photo));
                    }
                    jsonObject.put("photos", photosArr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final String body=jsonObject.toString();

                Future<Boolean> future=Utils.threadPool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            String jsonStr;
                            if(editingId<0) {
                                jsonStr = Utils.sendPost(Utils.baseURL+"/links", body);
                            }else{
                                jsonStr = Utils.sendPut(Utils.baseURL+"/links/"+editingId, body);
                            }
                            JSONObject jsonObj=new JSONObject(jsonStr);
                            System.out.println(jsonObj.toString());
                            final String result=jsonObj.getString("result");
                            final boolean succeeded=result.equals("Successful");
                            if(!succeeded) {
                                System.out.println(jsonStr);
                            }
                            return succeeded;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                });
                try {
                    Boolean succeeded=future.get();
                    Toast.makeText(AddActivity.this,succeeded?"Submit success":"Submit failed",Toast.LENGTH_SHORT).show();
                    if(succeeded){
                        setResult(RESULT_OK);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bundle extra = data.getExtras();
                    Bitmap bitmap = extra.getParcelable("data");
                    linkPhotos.add(bitmap);
                    listPhotos.setAdapter(adapter);
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    ContentResolver cr = this.getContentResolver();
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        linkPhotos.add(bitmap);
                        listPhotos.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
