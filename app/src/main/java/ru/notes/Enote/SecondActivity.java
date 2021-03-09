package ru.notes.Enote;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.TEXT_ALIGNMENT_TEXT_START;

public class SecondActivity extends AppCompatActivity {
private FloatingActionButton _addNote;
private LinearLayout _Listnote;
private ImageButton _Exit;
private ImageButton _Delete;

private boolean StatusError = false;

private String CurrentSession;

private String CurrentTextNote;

private ArrayList<Integer> IdButtons = new ArrayList<Integer>();
private ArrayList<Button> ButtonsArray = new ArrayList<Button>();

private boolean DeleteMode = false;

private  AsyncSendRequestTakeText sendRequestTakeText= new AsyncSendRequestTakeText();

private ArrayList<Blinking> blinks = new ArrayList<Blinking>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        CurrentSession = getIntent().getStringExtra("CookieSession");
        sendRequestTakeText.execute("http://35.228.102.189/api/notes",CurrentSession);
        onButtonClickSec();

    }
    public void onButtonClickSec ()
    {
        _Exit = (ImageButton) findViewById(R.id.buttonExit);
        _Delete = (ImageButton) findViewById((R.id.buttonBin));
        _Listnote = (LinearLayout) findViewById(R.id.Listnote);
        _addNote = (FloatingActionButton) findViewById(R.id.addnote);
     _addNote.setOnClickListener(
             new View.OnClickListener(){
                 @Override
                 public void onClick (View v) {
                     Intent intent = new Intent(SecondActivity.this, note_activity.class);
                     startActivityForResult(intent,1);
                    }
                 }
     );

     _Exit.setOnClickListener(
             new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     _Exit.setClickable(false);
                     try
                     {
                         AsyncSendRequestExit sendRequestExit = new AsyncSendRequestExit();
                         sendRequestExit.execute("http://35.228.102.189/api/logout",CurrentSession);
                     }
                     catch (Exception e) {
                         Toast.makeText(SecondActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                     }
                 }
             }
     );

        _Delete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(DeleteMode) {
                            for (Button but : ButtonsArray) {
                                but.setTextColor(Color.parseColor("#000000"));
                            }
                            _Delete.setImageAlpha(1000);
                            DeleteMode = false;
                            if(!blinks.isEmpty())
                                for (Blinking bl: blinks) {
                                    bl.interrupt();
                                }
                        }
                                    else {
                                        _Delete.setClickable(false);
                            DeleteMode = true;
                            Toast.makeText(SecondActivity.this, "Tap note to delete.", Toast.LENGTH_SHORT).show();
                            Blinking blinking = new Blinking("Blink");
                           blinks.add(blinking);
                           blinking.start();
                        }
                    }
                }
        );
    }
    //переопределение метода, срабатующего в момент выхода из окна note_activity (выполняется после выхода из метода startActivityForResult())
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode,resultCode,data);
        //добавление текста в новую кнопку
            if(requestCode==1&&resultCode==RESULT_OK) {
                CurrentTextNote = data.getStringExtra("editText");

                AsyncSendRequestAddText sendRequestAdd = new AsyncSendRequestAddText();
                sendRequestAdd.execute("http://35.228.102.189/api/addnote?text=",data.getStringExtra("editText"),CurrentSession);

            }
            if(requestCode==2&&resultCode==RESULT_OK){
                //изменения текста кнопки
                int id = data.getIntExtra("IdButton", -1);
                for (Button but:ButtonsArray) {
                    if(but.getId()==id) {
                        but.setText(data.getStringExtra("editText"));
                    break;
                    }
                }
                AsyncSendRequestChangeText sendRequestChange = new AsyncSendRequestChangeText();
                sendRequestChange.execute("http://35.228.102.189/api/updatenote/",String.valueOf(id) ,"?text=",data.getStringExtra("editText"),CurrentSession);
            }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(DeleteMode)
            {
                DeleteMode=false;
                return false;
            }
            else {
                AsyncSendRequestExit sendRequestExit = new AsyncSendRequestExit();
                sendRequestExit.execute("http://35.228.102.189/api/logout", CurrentSession);
                finishAffinity();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class AsyncSendRequestExit extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String [] params)
        {
            String responseServer = null;

            try
            {
                responseServer = HTTPConnect.SendWithCookie(params);
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
                Toast.makeText(SecondActivity.this, "Error with connection!", Toast.LENGTH_SHORT).show();
                _Exit.setClickable(true);
                return;
            }
            else {
                Toast.makeText(SecondActivity.this, "Goodbye!", Toast.LENGTH_SHORT).show();
                finish();
                _Exit.setClickable(true);
            }
        }
    }
    private class AsyncSendRequestTakeText extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String [] params)
        {
            String responseServer = null;

            try
            {
                responseServer = HTTPConnect.SendWithCookie(params);
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

            int response = HTTPConnect.takeStatus(message);
            switch (response)
            {
                case -4:
                    Toast.makeText(SecondActivity.this,"Session was interrupted!" , Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    if(HTTPConnect.takeCookies(message)!=null)
                        CurrentSession = HTTPConnect.takeCookies(message);
                    ArrayList<String> IdAndNotes = HTTPConnect.takeTextNotes(message);
                        Toast.makeText(SecondActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                        if(IdAndNotes==null)
                            return;
                    for(int i =0; i<IdAndNotes.size();i+=2)
                        {
                            IdButtons.add(Integer.parseInt(IdAndNotes.get(i)));
                        }

                        int delta = 1;
                        for(int i =1; i<IdAndNotes.size();i+=2)
                        {

                            final Button button = new Button(SecondActivity.this);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            button.setLayoutParams(layoutParams);
                            button.setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
                            button.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick (View v) {
                                    if(DeleteMode)
                                    {
                                        AsyncSendRequestDeleteText requestDeleteText = new AsyncSendRequestDeleteText();
                                        requestDeleteText.execute("http://35.228.102.189/api/deletenote/",String.valueOf(button.getId()),CurrentSession);
                                        if(StatusError)
                                            StatusError = false;
                                        else
                                        {
                                            for (int i = 0; i < IdButtons.size(); ++i) {
                                                if (IdButtons.get(i) == button.getId()) {
                                                    ButtonsArray.remove(i);
                                                    IdButtons.remove(i);
                                                    break;
                                                }
                                            }
                                            _Listnote.removeView(button);
                                        }
                                    }
                                    else {
                                        int id = button.getId();
                                        Intent intent = new Intent(SecondActivity.this, note_activity.class);
                                        String textToUpdate = button.getText().toString();
                                        intent.putExtra("textToUpdate", textToUpdate);
                                        intent.putExtra("IdButton", id);
                                        startActivityForResult(intent, 2);
                                    }
                                }
                            });
                            button.setId(IdButtons.get(i-delta));
                            button.setTextSize(20);
                            button.setMaxLines(1);
                            button.setEllipsize(TextUtils.TruncateAt.END);
                            button.setAllCaps(false);
                            button.setText(IdAndNotes.get(i));
                            _Listnote.addView(button);
                            ButtonsArray.add(button);
                            delta++;
                        }
                    break;
                default :
                    break;
            }
        }
    }
    private class AsyncSendRequestAddText extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String [] params)
        {
            String responseServer = null;

            try
            {
                responseServer = HTTPConnect.SendWithCookie(params);
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
                Toast.makeText(SecondActivity.this, "Error with connection!", Toast.LENGTH_SHORT).show();
                return;
            }
           int response = HTTPConnect.takeStatus(message);
            switch (response)
            {
                case 1:
                    if(HTTPConnect.takeCookies(message)!=null)
                        CurrentSession = HTTPConnect.takeCookies(message);

                    int CurrentId=HTTPConnect.takeIdNote(message);
                        //создание кнопки
                    Button button = new Button(SecondActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    button.setLayoutParams(layoutParams);
                    button.setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
                    //реализация функции кнопки
                    button.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick (View v) {
                            if(DeleteMode)
                            {
                                AsyncSendRequestDeleteText requestDeleteText = new AsyncSendRequestDeleteText();
                                requestDeleteText.execute("http://35.228.102.189/api/deletenote/",String.valueOf(button.getId()),CurrentSession);
                               if(StatusError)
                                   StatusError = false;
                               else
                                {
                                    for (int i = 0; i < IdButtons.size(); ++i) {
                                        if (IdButtons.get(i) == button.getId()) {
                                            ButtonsArray.remove(i);
                                            IdButtons.remove(i);
                                            break;
                                        }
                                    }
                                    _Listnote.removeView(button);
                                }
                            }
                            else
                                {
                                int id = button.getId();
                                Intent intent = new Intent(SecondActivity.this, note_activity.class);
                                String textToUpdate = button.getText().toString();
                                intent.putExtra("textToUpdate", textToUpdate);
                                intent.putExtra("IdButton", id);
                                startActivityForResult(intent, 2);
                            }

                        }
                    });
                    button.setAllCaps(false);
                    button.setTextSize(20);
                    button.setMaxLines(1);
                    button.setEllipsize(TextUtils.TruncateAt.END);
                    button.setText(CurrentTextNote);
                    IdButtons.add(CurrentId);
                    button.setId(CurrentId);
                    _Listnote.addView(button);
                    ButtonsArray.add(button);
                    CurrentTextNote=null;
                    break;
                case -4:
                    Toast.makeText(SecondActivity.this,"Error! Note wasn't added!" , Toast.LENGTH_SHORT).show();
                    break;

                default :
                    break;
            }
            if(HTTPConnect.takeCookies(message)!=null)
                CurrentSession = HTTPConnect.takeCookies(message);
        }
    }
    private class AsyncSendRequestChangeText extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String [] params)
        {
            String responseServer = null;

            try
            {
                responseServer = HTTPConnect.SendWithCookie(params);
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
                Toast.makeText(SecondActivity.this, "Error with connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            int response = HTTPConnect.takeStatus(message);
            switch (response)
            {
                case -4:
                case -5:
                case -6:
                    Toast.makeText(SecondActivity.this,message , Toast.LENGTH_SHORT).show();
                    break;

                default :
                    break;
            }
            if(HTTPConnect.takeCookies(message)!=null)
                CurrentSession = HTTPConnect.takeCookies(message);

        }
    }
    private class AsyncSendRequestDeleteText extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String [] params)
        {
            String responseServer = null;

            try
            {
                responseServer = HTTPConnect.SendWithCookie(params);
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
                Toast.makeText(SecondActivity.this, "Error with connection!", Toast.LENGTH_SHORT).show();
                return;
            }
            int response = HTTPConnect.takeStatus(message);
            switch (response)
            {
                case 1:
                    if(HTTPConnect.takeCookies(message)!=null)
                        CurrentSession = HTTPConnect.takeCookies(message);
                    break;
                case -4:
                case -5:
                case -6:
                    Toast.makeText(SecondActivity.this,"Error! Note wasn't deleted!" , Toast.LENGTH_SHORT).show();
                    StatusError = true;
                    break;
                default :
                    break;
            }
            if(HTTPConnect.takeCookies(message)!=null)
                CurrentSession = HTTPConnect.takeCookies(message);
        }
    }

    private class Blinking extends Thread{
        Blinking(String name)
        {
            super(name);
        }

         public void run(){

            while (DeleteMode) {
                for (Button but : ButtonsArray) {
                    but.setTextColor(Color.parseColor("#ff0000"));
                }
                _Delete.setImageAlpha(0);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (Button but : ButtonsArray) {
                    but.setTextColor(Color.parseColor("#000000"));
                }
                _Delete.setImageAlpha(1000);

                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(!_Delete.isClickable())
                _Delete.setClickable(true);
            }
        }
    }
}

