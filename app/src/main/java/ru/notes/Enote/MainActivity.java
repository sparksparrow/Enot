package ru.notes.Enote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
//Объявление элементов
    private Button _enter ;
    private Button _create_new_account;
    private TextView _login;
    private  TextView _password;
    private final int MY_PERMISSIONS_REQUEST_INTERNET = 100;
//конструктор формы (вызывается метод onButtonClick(), который инициализирует элементы в переменные, а также прописывает им метода взаимодействия)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onButtonClick();
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        }
        }
//Инициализирует элементы и их методы
    public void onButtonClick()
    {
        _login = (TextView) findViewById(R.id.login);
        _create_new_account = (Button) findViewById(R.id.create_new_account);
        _enter=(Button)findViewById(R.id.Sign_in);
        _password = (TextView) findViewById((R.id.password));
        _enter.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View v){
                        if(_login.length()<3||_password.length()<5){
                            Toast.makeText(MainActivity.this, "Login shouldn't be less than 3 symbols.\nPassword shouldn't be less than 5 symbols.", Toast.LENGTH_SHORT).show();
                            return;
                        }
//ограничение на ввод
                        for (char ch : _login.getText().toString().toCharArray())
                            if ((int)ch <(int)'0'||(int)ch>(int)'9'&&(int) ch < (int) 'A' || (int) ch > (int) 'Z' && (int) ch < (int) 'a' || (int) ch > (int) 'z') {
                                Toast.makeText(MainActivity.this, "You can only use numbers and english words!", Toast.LENGTH_SHORT).show();
                                return;
                            }
//запрос на авторизацию
                        try
                        {
                            AsyncSendRequest sendRequest = new AsyncSendRequest();
                            sendRequest.execute("http://35.228.102.189/api/login?login=", _login.getText().toString(), "&password=", _password.getText().toString());
                        }
                        catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        _password.setText("");
                        _enter.setClickable(false);
                    }
                }
        );
//открывает новую форму с регистрацией
        _create_new_account.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _create_new_account.setClickable(false);
                        Intent intentRed = new Intent(".activity_reg");
                        startActivity(intentRed);
                        _create_new_account.setClickable(true);
                    }
                }
        );
    }
//Асинхронный поток, выполняющий запрос на сервер (все запросы прописаны в статическом классе HTTPConnect, здесь только обработка ответов)
//doInBackground - выполняется асинхронно, содержит метод запроса, onPostExecute выполняется после doInBackground, здесь обработка ответа)
    private class AsyncSendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String [] params)
        {
            String responseServer = null;

            try
            {
                responseServer = HTTPConnect.Send(params);
            }
            catch (Exception ex)
            {
                return ex.getMessage();
            }
            return responseServer;
        }

        @Override
        protected void onPostExecute(String message)
        {
            if(message == null) {
                Toast.makeText(MainActivity.this, "Error with connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            int response = HTTPConnect.takeStatus(message);
            switch (response)
            {
                case -3:
                    Toast.makeText(MainActivity.this,"Incorrect login or password!" , Toast.LENGTH_SHORT).show();
                    break;

                case -1:
                    Toast.makeText(MainActivity.this,"You're logged in already!" , Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Intent intent = new Intent(".SecondActivity");
                    //передает сессионные куки в следующую форму
                    intent.putExtra("CookieSession",HTTPConnect.takeCookies(message));
                    startActivity(intent);
                    break;
                default :
                    break;
            }
            _enter.setClickable(true);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET:
                if (grantResults.length <= 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "Application doesn't work without Internet", Toast.LENGTH_SHORT).show();
                finish();
                }
                return;
        }
    }
}
