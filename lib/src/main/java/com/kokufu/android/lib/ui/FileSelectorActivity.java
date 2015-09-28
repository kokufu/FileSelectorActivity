/*
 * Copyright (C) 2015 Yusuke Miura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kokufu.android.lib.ui;

import com.kokufu.android.lib.ui.fileselectoractivity.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * All public method must be called on UI thread.
 */
public class FileSelectorActivity extends Activity {
    @SuppressWarnings("unused")
    public enum SelectionType {
        FILE,
        DIR
    }

    public static final String ARG_DIR = "dir";
    public static final String ARG_TYPE = "type";
    public static final String RESULT_FILE = "file";

    private TextView mPathView;
    private ListView mListView;
    private ProgressBar mProgress;
    private File mDir;
    private SelectionType mType = SelectionType.FILE;
    private FileLoadTask mFileLoadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);

        mPathView = (TextView) findViewById(R.id.textView1);
        mListView = (ListView) findViewById(R.id.listView);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        mPathView.setHorizontallyScrolling(true);
        mPathView.setMovementMethod(ScrollingMovementMethod.getInstance());

        mListView.setOnItemClickListener(mOnItemClickListener);

        // Type
        if (getIntent() != null) {
            Serializable type = getIntent().getSerializableExtra(ARG_TYPE);
            if (type != null) {
                mType = (SelectionType) type;
            }
        }

        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(null);
            }
        });
        Button buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setVisibility((mType == SelectionType.DIR) ? View.VISIBLE : View.GONE);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(mDir);
            }
        });

        // Dir
        File dir = null;
        if (savedInstanceState != null) {
            dir = (File) savedInstanceState.getSerializable(ARG_DIR);
        } else if (getIntent() != null) {
            dir = (File) getIntent().getSerializableExtra(ARG_DIR);
        }
        if (dir == null) {
            dir = Environment.getExternalStorageDirectory();
            if (dir == null || !dir.exists()) {
                dir = Environment.getRootDirectory();
            }
        }
        setDir(dir);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARG_DIR, mDir);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (!moveToParentDir()) {
            super.onBackPressed();
        }
    }

    public void setDir(File dir) {
        if (mFileLoadTask != null) {
            mFileLoadTask.cancel(true);
        }
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            Toast.makeText(this,
                    getString(R.string.error_dir_access),
                    Toast.LENGTH_SHORT).show();
        } else {
            mFileLoadTask = new FileLoadTask(dir);
            mFileLoadTask.execute();
        }
    }

    /**
     *
     * @return {@code false} when there is no parent.
     */
    public boolean moveToParentDir() {
        if (mDir == null || mDir.getParentFile() == null) {
            return false;
        }
        setDir(mDir.getParentFile());
        return true;
    }

    private void select(File file) {
        if (file != null) {
            Intent data = new Intent();
            data.putExtra(RESULT_FILE, file);
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File f = (File) parent.getItemAtPosition(position);

            if (f.isDirectory()) {
                setDir(f);
            } else if (mType == SelectionType.FILE){
                select(f);
            }
        }
    };

    private class FileLoadTask extends AsyncTask<Void, Void, List<File>> {
        private final File mLoadingDir;

        /**
         * @param dir to be loaded. it must not be {@code null}
         */
        public FileLoadTask(File dir) {
            mLoadingDir = dir;
        }

        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<File> doInBackground(Void... params) {
            if (mLoadingDir == null || mLoadingDir.listFiles() == null) {
                return null;
            }

            List<File> dirs = new ArrayList<>();
            List<File> files = new ArrayList<>();
            for (File f : mLoadingDir.listFiles()) {
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    files.add(f);
                }
            }
            Collections.sort(dirs);
            Collections.sort(files);
            if (mType == SelectionType.FILE) {
                dirs.addAll(files);
            }
            return dirs;
        }

        @Override
        protected void onPostExecute(List<File> result) {
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
            if (isCancelled()) {
                return;
            }
            if (mPathView == null || mListView == null) {
                return;
            }
            mDir = mLoadingDir;
            mPathView.setText(mDir.getPath());
            if (result == null) {
                Toast.makeText(FileSelectorActivity.this,
                        getString(R.string.error_dir_access),
                        Toast.LENGTH_SHORT).show();
                mListView.setAdapter(null);
            } else if (result.size() == 0) {
                // setEmptyText(getString(R.string.empty_dir));
                mListView.setAdapter(null);
            } else {
                mListView.setAdapter(new FileListAdapter(FileSelectorActivity.this, result));
            }
            mFileLoadTask = null;
        }
    }

    private static class FileListAdapter extends BaseAdapter {
        private DateFormat mDateFormat;
        private DateFormat mTimeFormat;
        private final Context mContext;
        private final List<File> mFiles;

        public FileListAdapter(Context context, List<File> files) {
            mContext = context;
            mFiles = files;
            mDateFormat = android.text.format.DateFormat.getDateFormat(context);
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        }

        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return mFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.list_item_file, parent, false);
            }

            ImageView iconView = (ImageView) convertView.findViewById(R.id.imageView1);
            TextView tv1 = (TextView) convertView.findViewById(R.id.textView1);
            TextView tv2 = (TextView) convertView.findViewById(R.id.textView2);

            int textColor = tv1.getTextColors().getDefaultColor();

            File file = (File) getItem(position);
            if (isColorDark(textColor)) {
                if (file.isDirectory()) {
                    iconView.setImageResource(R.drawable.ic_dir_black);
                } else {
                    iconView.setImageResource(R.drawable.ic_doc_black);
                }
            } else {
                if (file.isDirectory()) {
                    iconView.setImageResource(R.drawable.ic_dir_white);
                } else {
                    iconView.setImageResource(R.drawable.ic_doc_white);
                }
            }

            tv1.setText(file.getName());

            Date lastModifyDate = new Date(file.lastModified());
            tv2.setText(mDateFormat.format(lastModifyDate) +
                    " " +
                    mTimeFormat.format(lastModifyDate));

            return convertView;
        }
    }

    private static boolean isColorDark(int color){
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
