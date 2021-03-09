package ru.notes.Enote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

public class note_activity extends AppCompatActivity {

private EditText _editText;
int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_activity);

        _editText = (EditText) findViewById(R.id.editText);
        if(getIntent().getStringExtra("textToUpdate")!=null)
        _editText.setText(getIntent().getStringExtra("textToUpdate"));

            id=getIntent().getIntExtra("IdButton", -1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            getIntent().putExtra("editText",_editText.getText().toString());
            setResult(RESULT_OK, getIntent());
            getIntent().putExtra("IdButton",id);

            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
