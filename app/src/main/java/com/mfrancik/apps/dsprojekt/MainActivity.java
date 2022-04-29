package com.mfrancik.apps.dsprojekt;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.animation.ArgbEvaluatorCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
	private final HashMap<Integer, Consumer<Integer>> afterPermissionGranted = new HashMap<Integer, Consumer<Integer>>(){};
	private String filePath;
	private File mainDir;

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LinearLayout cameraButton = findViewById(R.id.cameraButton);
		LinearLayout albumsButton = findViewById(R.id.albumsButton);
		LinearLayout collageButton = findViewById(R.id.collageButton);
		LinearLayout networkingButton = findViewById(R.id.networkingButton);

		for (File file : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).listFiles()){
			if (file.getName().equals("MateuszFrancik")) {
				mainDir = file;
				this.filePath = file.getPath();
			}
		}

		cameraButton.setOnClickListener(v -> {
			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			alert.setTitle("Photo source");
			alert.setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
				switch (which) {
					case 0:
						try {
							intentWithPermission(Manifest.permission.CAMERA, 101, (a) -> {
								Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								if (intent.resolveActivity(getPackageManager()) != null) {
									startActivityForResult(intent, 200);
								}
							});
						} catch (InvocationTargetException | IllegalAccessException e) {
							e.printStackTrace();
						}
						break;
					case 1:
						try {
							intentWithPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100, (e) -> {
								Intent intent = new Intent(Intent.ACTION_PICK);
								intent.setType("image/*");
								startActivityForResult(intent, 201);
							});
						} catch (InvocationTargetException | IllegalAccessException e) {
							e.printStackTrace();
						}
						break;
				}
			});
			alert.show();
		});

		albumsButton.setOnClickListener(v -> {
			try {
				intentWithPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100, (a) -> {
					Intent intent = new Intent(MainActivity.this, Albums.class);
					startActivity(intent);
				});
			} catch (InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});

		albumsButton.setOnClickListener(v -> {
			try {
				intentWithPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100, (e) -> {
					Intent intent = new Intent(MainActivity.this, Albums.class);
					startActivity(intent);
				});
			} catch (InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});

		collageButton.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, Collage.class);
			startActivity(intent);
		});

		networkingButton.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, Network.class);
			startActivity(intent);
		});
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void intentWithPermission(String permission, int requestCode, Consumer<Integer> function) throws InvocationTargetException, IllegalAccessException {
		afterPermissionGranted.put(requestCode, function);
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
		} else {
			Toast.makeText(MainActivity.this, String.format("Permission '%s' already granted", permission), Toast.LENGTH_SHORT).show();
			Objects.requireNonNull(afterPermissionGranted.get(requestCode)).accept(0);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch (requestCode) {
				case 100:
					File pic = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );

					File x = new File(pic, "MateuszFrancik");
					x.mkdir();
					this.filePath = x.getPath();
					this.mainDir = x;
					new File(x, "people").mkdir();
					new File(x, "places").mkdir();
					new File(x, "things").mkdir();

					Objects.requireNonNull(afterPermissionGranted.get(requestCode)).accept(0);
					break;
				case 101:
					Objects.requireNonNull(afterPermissionGranted.get(requestCode)).accept(0);
					break;
			}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 200:
				if (resultCode == RESULT_OK) {
					assert data != null;
					Bundle extras = data.getExtras();
					handleSaving((Bitmap) extras.get("data"));
				}
				break;
			case 201:
				if  (resultCode == RESULT_OK) {
					assert data != null;
					Uri imgData = data.getData();
					InputStream inputStream = null;
					try {
						inputStream = getContentResolver().openInputStream(imgData);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					handleSaving(BitmapFactory.decodeStream(inputStream));
				}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	void handleSaving(Bitmap bitmap) {
		AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setTitle("Photo destination");
		String[] destinations = Stream.of(this.mainDir.listFiles()).map(File::getName).toArray(String[]::new);
		alert.setItems(destinations, (dialog, which) -> {
			String dirName = destinations[which];
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			byte[] byteArray = outputStream.toByteArray();
			FileOutputStream fs;
			try {
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String d = df.format(new Date());
				String x = this.filePath + "/" + dirName + "/" + d + ".jpg";
				Log.d("DIS-TAG", x);
				fs = new FileOutputStream(x);
				fs.write(byteArray);
				fs.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		});
		alert.show();
	}

}