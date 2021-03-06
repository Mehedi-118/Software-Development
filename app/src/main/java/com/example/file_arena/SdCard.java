package com.example.file_arena;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class SdCard extends AppCompatActivity {

    ListView lview;
    ArrayList<String> clickedpath = new ArrayList<String>();
    int count = 0;
    public static String[] getStorageDirectories(Context pContext) {

        final Set<String> rv = new HashSet<>();


        File[] listExternalDirs = ContextCompat.getExternalFilesDirs(pContext, null);
        for (int i = 0; i < listExternalDirs.length; i++) {
            if (listExternalDirs[i] != null) {
                String path = listExternalDirs[i].getAbsolutePath();
                int indexMountRoot = path.indexOf("/Android/data/");
                if (indexMountRoot >= 0 && indexMountRoot <= path.length()) {

                    rv.add(path.substring(0, indexMountRoot));
                }
            }
        }
        return rv.toArray(new String[rv.size()]);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF0000"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        setContentView(R.layout.activity_sd_card);

        lview = findViewById(R.id.lview1);
        final String[] list = getStorageDirectories(SdCard.this);

        File f = new File(list[0]);
        final File[] paths = f.listFiles();

        String[] values = f.list();

        //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, values);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, R.id.list_content, values);

        lview.setAdapter(adapter);//setting the adapter
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String s = paths[position].getAbsolutePath();

                File f = new File(s);

                if (f.isDirectory()) {
                    Intent myIntent = new Intent(SdCard.this, SubfolderView.class);
                    myIntent.putExtra("subfolder", s);
                    startActivity(myIntent);
                    Toast.makeText(SdCard.this, s, Toast.LENGTH_SHORT).show();
                } else {
                    // MimeTypeMap myMime=MimeTypeMap.getSingleton();
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);

                    newIntent.setDataAndType(Uri.fromFile(f), URLConnection.guessContentTypeFromName(s));

                    Intent j = Intent.createChooser(newIntent, "Choose an application to open with: ");
                    startActivity(j);
                }

            }
        });
        lview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lview.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                if (checked)

                    count++;
                if (!checked) {
                    count--;
                }

                mode.setTitle(count + "Selected");


            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.long_clicked_menu_item, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ShareId: {

                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        Uri screenshotUri = Uri.parse(list[0]);
                        sharingIntent.setType("*/*");
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                        startActivity(Intent.createChooser(sharingIntent, "Share With:"));
                        return true;

                    }
                    case R.id.Deleteid: {
                        for (int i = 0; i != clickedpath.size(); i++) {
                            File f = new File(clickedpath.get(i));
                            f.delete();
                        }
                        Toast.makeText(SdCard.this, "deleted", Toast.LENGTH_SHORT).show();
                        return true;

                    }
                    case R.id.CopyId: {
                        String cp = "";
                        for (int i = 0; i != clickedpath.size(); i++) {
                            cp += clickedpath.get(i) + "|";
                        }
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("File path", cp);
                        clipboard.setPrimaryClip(clip);
                        return true;
                    }
                    case R.id.pasteId: {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        String pasteData = "";

                        // If it does contain data, decide if you can handle the data.
                        if (!(clipboard.hasPrimaryClip())) {

                        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {

                            // since the clipboard has data but it is not plain text

                        } else {

                            //since the clipboard contains plain text.
                            ClipData.Item item2 = clipboard.getPrimaryClip().getItemAt(0);

                            // Gets the clipboard as text.
                            pasteData = item2.getText().toString();
                        }
                        String[] files=pasteData.split("\\|");
                        String[] list=getStorageDirectories(SdCard.this);

                        for(int i=0;i!=files.length;i++) {
                            Toast.makeText(SdCard.this,files[i],Toast.LENGTH_SHORT).show();
                            Toast.makeText(SdCard.this,list[0],Toast.LENGTH_SHORT).show();
                            try {
                                copyDirectoryOneLocationToAnotherLocation(new File(files[i]),new File(list[0]));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });


    }

    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }

    /**
     * MENU ITEM WITH LISTENER
     */

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.newFolderId: {
                newFolder();

                return true;
            }
            case R.id.pasteId: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = "";

                // If it does contain data, decide if you can handle the data.
                if (!(clipboard.hasPrimaryClip())) {

                } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {

                    // since the clipboard has data but it is not plain text

                } else {

                    //since the clipboard contains plain text.
                    ClipData.Item item2 = clipboard.getPrimaryClip().getItemAt(0);

                    // Gets the clipboard as text.
                    pasteData = item2.getText().toString();
                }
                String[] files=pasteData.split("\\|");
                String[] list=getStorageDirectories(SdCard.this);

                for(int i=0;i!=files.length;i++) {
                    Toast.makeText(SdCard.this,files[i],Toast.LENGTH_SHORT).show();
                    //Toast.makeText(SdCard.this,list[]+"/",Toast.LENGTH_SHORT).show();
                    try {
                        copyDirectoryOneLocationToAnotherLocation(new File(files[i]),new File(list[0]+"/myfold"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void newFolder() {
        final String[] list = getStorageDirectories(SdCard.this);


        File f = new File(list[0]);
        final String path = list[0];
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SdCard.this);
        View mView = getLayoutInflater().inflate(R.layout.floating_edit_textwindow, null);
        final EditText FileName = mView.findViewById(R.id.write);
        Button Done = mView.findViewById(R.id.DoneID);
        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FileName.getText().toString().isEmpty()) {

                    File theDir = new File(list[1] + "//new folder");
                    ActivityCompat.requestPermissions(SdCard.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    if (!theDir.exists()) {
                        try {
                            theDir.mkdirs();
                            Toast.makeText(SdCard.this, "New Folder created", Toast.LENGTH_SHORT).show();
                            SdCard.this.finish();
                            Intent InternalStorage = new Intent(SdCard.this, SdCard.class);
                            startActivity(InternalStorage);

                        } catch (SecurityException se) {
                            Toast.makeText(SdCard.this, "Cannot create folder here", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        int i = 0;
                        while (true) {
                            File theDirx = new File(path + "//new folder" + "(" + (++i) + ")");
                            if (!theDirx.exists()) {
                                try {
                                    theDirx.mkdir();
                                    Toast.makeText(SdCard.this, "new folder" + "(" + (i) + ")" + " created", Toast.LENGTH_SHORT).show();
                                    SdCard.this.finish();
                                    Intent InternalStorage = new Intent(SdCard.this, SdCard.class);
                                    startActivity(InternalStorage);
                                    break;
                                } catch (SecurityException se) {
                                    Toast.makeText(SdCard.this, "Cannot create folder here", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }

                        }

                    }
                } else {
                    String folderName = FileName.getText().toString();
                    File theDir = new File(path + "//" + folderName);
                    if (!theDir.exists()) {
                        try {
                            theDir.mkdir();
                            Toast.makeText(SdCard.this, "\"" + folderName + "\" folder created", Toast.LENGTH_SHORT).show();
                            SdCard.this.finish();
                            Intent InternalStorage = new Intent(SdCard.this, SdCard.class);
                            startActivity(InternalStorage);

                        } catch (SecurityException se) {
                            Toast.makeText(SdCard.this, "Cannot create folder here", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SdCard.this, "A folder named \"" + folderName + "\" already exists", Toast.LENGTH_SHORT).show();
                    }


                }

            }
        });
        mBuilder.setView(mView);
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }


}
