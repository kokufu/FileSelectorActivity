FileSelectorActivity
====================
Simple File or Dir selector dialog for Android.

## How to use
First, import the library. If you use Android Studio (Gradle), you can do it like below.

build.gradle
```gradle
repositories {
    maven { url 'http://kokufu.github.io/maven-repo' }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.kokufu.android.lib.ui:fileselectoractivity-aar:0.1'
}
```

Then, You can use it like below.

AndroidManifest.xml
```xml
<activity
    android:name="com.kokufu.android.lib.ui.FileSelectorActivity">
</activity>
```

MainActivity.java
```java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileSelectorActivity.class);
                intent.putExtra(FileSelectorActivity.ARG_TYPE, FileSelectorActivity.SelectionType.FILE);
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
}
```
