package com.mfrancik.apps.dsprojekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
	private LinearLayout cameraButton;
	private LinearLayout albumsButton;
	private LinearLayout collageButton;
	private LinearLayout networkingButton;
	private boolean unlockGallery = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cameraButton = findViewById(R.id.cameraButton);
		albumsButton = findViewById(R.id.albumsButton);
		collageButton = findViewById(R.id.collageButton);
		networkingButton = findViewById(R.id.networkingButton);

		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, Camera.class);
				startActivity(intent);
			}
		});

		albumsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100);
				if (unlockGallery) {
					Intent intent = new Intent(MainActivity.this, Albums.class);
					startActivity(intent);
				}
			}
		});

		collageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, Collage.class);
				startActivity(intent);
			}
		});

		networkingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, Network.class);
				startActivity(intent);
			}
		});
	}

	public void checkPermission(String permission, int requestCode) {
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
		} else {
			this.unlockGallery = true;
			Toast.makeText(MainActivity.this, String.format("Permission '%s' already granted", permission), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 100 && grantResults.length > 0 &&
				grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			File pic = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );

			new File(pic, "people").mkdir();
			new File(pic, "places").mkdir();
			new File(pic, "things").mkdir();

			this.unlockGallery = true;
		}
	}
}