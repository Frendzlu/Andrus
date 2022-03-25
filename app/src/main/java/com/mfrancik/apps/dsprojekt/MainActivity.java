package com.mfrancik.apps.dsprojekt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
	private LinearLayout cameraButton;
	private LinearLayout albumsButton;
	private LinearLayout collageButton;
	private LinearLayout networkingButton;
	private boolean unlockGallery = false;
	private boolean unlockCamera = false;
	private String filePath;

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
				checkPermission(Manifest.permission.CAMERA, 101);
				if (unlockCamera) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					if (intent.resolveActivity(getPackageManager()) != null) {
						startActivityForResult(intent, 200);
					}
				}
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
			switch (requestCode) {
				case 100:
					this.unlockGallery = true;
					break;
				case 101:
					this.unlockCamera = true;
					break;
			}
			Toast.makeText(MainActivity.this, String.format("Permission '%s' already granted", permission), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch (requestCode) {
				case 100:
					File pic = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );

					File x = new File(pic, "MateuszFrancik");
					this.filePath = x.getPath();
					Log.d("TAG", this.filePath);
					new File(x, "people").mkdir();
					new File(x, "places").mkdir();
					new File(x, "things").mkdir();

					this.unlockGallery = true;
					break;
				case 101:
					this.unlockCamera = true;
					break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 200) {
			if (resultCode == RESULT_OK) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				Bundle extras = data.getExtras();
				Bitmap bitmappe = (Bitmap) extras.get("data");
				bitmappe.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] byteArray = stream.toByteArray();

				FileOutputStream fs = null;
				try {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
					String d = df.format(new Date());

					fs = new FileOutputStream(d + ".jpg");
					fs.write(byteArray);
					fs.close();
				} catch (IOException e){
					Log.d("TAG", String.valueOf(e));
				}
			}
		}
	}
}