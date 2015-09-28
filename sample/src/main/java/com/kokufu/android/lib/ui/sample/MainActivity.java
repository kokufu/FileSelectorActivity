package com.kokufu.android.lib.ui.sample;

import com.kokufu.android.lib.ui.FileSelectorActivity;
import com.kokufu.android.lib.ui.sample.fileselectoractivity.R;

//import com.kokufu.android.lib.ui.fileselectoractivity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileSelectorActivity.class);
                intent.putExtra(FileSelectorActivity.ARG_TYPE, getSelectionType());
                intent.putExtra(FileSelectorActivity.ARG_DIR, Environment.getExternalStorageDirectory());
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    File f = (File) data.getSerializableExtra(FileSelectorActivity.RESULT_FILE);
                    ((TextView) findViewById(R.id.textView1)).setText(f.getAbsolutePath());
                } else {
                    ((TextView) findViewById(R.id.textView1)).setText(null);
                }
        }
    }

    private FileSelectorActivity.SelectionType getSelectionType() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String selected = (String) spinner.getSelectedItem();
        switch (selected) {
            case "Dir":
                return FileSelectorActivity.SelectionType.DIR;
            case "File":
                return FileSelectorActivity.SelectionType.FILE;
            default:
                throw new IllegalArgumentException("Spinner String is wrong.");
        }
    }
}
