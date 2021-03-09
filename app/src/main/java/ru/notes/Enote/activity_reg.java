package ru.notes.Enote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class activity_reg extends AppCompatActivity {
    private TextView _login_reg;
    private Button _create_account_reg;
    private TextView _password_reg;
    private TextView _confirm_password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        onButtonClickReg();
    }

    public void onButtonClickReg()
    {
        _login_reg = (TextView) findViewById(R.id.login_reg);
        _create_account_reg = (Button) findViewById(R.id.create_account);
        _password_reg = (TextView) findViewById((R.id.password_reg));
        _confirm_password = (TextView) findViewById((R.id.confirm_password));

        _create_account_reg.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AsyncSendRequest sendRequest = new AsyncSendRequest();
                        if(_login_reg.length()<3||_password_reg.length()<5){
                            Toast.makeText(activity_reg.this, "Login shouldn't be less than 3 symbols.\nPassword shouldn't be less than 5 symbols.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (char ch : _login_reg.getText().toString().toCharArray())
                            if ((int)ch <(int)'0'||(int)ch>(int)'9'&&(int) ch < (int) 'A' || (int) ch > (int) 'Z' && (int) ch < (int) 'a' || (int) ch > (int) 'z') {
                                Toast.makeText(activity_reg.this, "You can only use numbers and english words!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        if(!_password_reg.getText().toString().equals(_confirm_password.getText().toString())) {
                            Toast.makeText(activity_reg.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            sendRequest.execute("http://35.228.102.189/api/register?login=", _login_reg.getText().toString(), "&password=", _password_reg.getText().toString());
                        }
                        catch (Exception e) {
                            Toast.makeText(activity_reg.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );

        }
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
        protected void onPostExecute(String message){
            if(message == null) {
                Toast.makeText(activity_reg.this, "Error with connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            int response = HTTPConnect.takeStatus(message);
            switch (response)
            {
                case -2:
                    Toast.makeText(activity_reg.this,"This login is busy!" , Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(activity_reg.this,"You're logged in already!" , Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(activity_reg.this,"Registration completed successfully!" , Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default :
                    break;
            }

        }
    }
}
