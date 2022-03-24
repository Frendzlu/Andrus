package com.mfrancik.apps.dsprojekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.HashMap;

public class Albums extends AppCompatActivity {
    public ListView albumsList;
    public ImageView addButton;
    private File pic;
    private HashMap<String, File> fileMap;
    private String[] fileMapKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        albumsList = findViewById(R.id.albumsListView);
        addButton = findViewById(R.id.addDirectoryButton);
        ActionBar actionBar =  getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        fileMap = new HashMap<String, File>();

        this.pic = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );
        for (File file : pic.listFiles()){
            fileMap.put(file.getName(), file);
        }

        createAdapter();

        albumsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("TAG","numer klikanego wiersza w ListView = " + i);
            }
        });

        albumsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Albums.this);
                alert.setTitle("Usuń");
                alert.setMessage("Czy na pewno chcesz usunąć ten folder?");
                alert.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File dir = fileMap.get(fileMapKeys[i]);
                        for (File file : dir.listFiles()){
                            file.delete();
                        }
                        dir.delete();
                        fileMap.remove(fileMapKeys[i]);
                        createAdapter();
                    }
                });
                alert.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //wyświetl which
                    }
                });
                alert.show();
                return true;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Albums.this);
                alert.setTitle("Nowy folder");
                alert.setMessage("Podaj nazwę nowego folderu:");
                EditText input = new EditText(Albums.this);
                input.setText("nazwa folderu");
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(pic, input.getText().toString());
                        file.mkdir();
                        fileMap.put(file.getName(), file);
                        createAdapter();
                    }
                });
                alert.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //wyświetl which
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAdapter() {
        fileMapKeys = fileMap.keySet().toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                Albums.this,
                R.layout.list_row,
                R.id.albumName,
                fileMapKeys
        );
        albumsList.setAdapter(adapter);
    }
}