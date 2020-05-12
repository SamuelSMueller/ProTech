package com.example.android.ProTech.vpnService;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.support.annotation.NonNull;

import com.example.android.ProTech.keyActivity;
import com.example.android.ProTech.R;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class vPNActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vpn);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (networkAndAirplaneModeCheck()) {
			startVPN();
		} else {
			showInfoDialog(getResources().getString(R.string.app_name),
					getResources().getString(R.string.no_network_information));
		}
	}



	private void startVPN() {
		try {
			if (!checkForActiveInterface("tun0")) {

				Intent intent = VpnService.prepare(this);
				if (intent != null) {

					startActivityForResult(intent, 0);
				} else {

					onActivityResult(0, RESULT_OK, null);
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private boolean checkForActiveInterface(String networkInterfaceName) throws Exception {
		List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
		for (NetworkInterface networkInterface : interfaces) {
			if (networkInterface.getName().equals(networkInterfaceName)) {
				return networkInterface.isUp();
			}
		}
		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Intent captureVpnServiceIntent = new Intent(getApplicationContext(), vPNService.class);
			startService(captureVpnServiceIntent);
		} else if (resultCode == RESULT_CANCELED) {
			showVPNRefusedDialog();
		}
	}

	private void showVPNRefusedDialog() {
		new AlertDialog.Builder(this)
				.setTitle("Usage Alert")
				.setMessage("You must trust the application in order to run a VPN based trace.")
				.setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startVPN();
					}
				})
				.setNegativeButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.show();

	}

	private void showInfoDialog(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private boolean isConnectedToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			}
		}
		return false;
	}

	private boolean networkAndAirplaneModeCheck() {
		return isConnectedToInternet();
	}

	public void enableKeyboard(View view){
		Intent intent = new Intent(this, keyActivity.class);
		startActivity(intent);
	}

	public void checkCon(View view) {
			if (networkAndAirplaneModeCheck())
				startVPN();
			else {
				showInfoDialog(getResources().getString(R.string.app_name),
						getResources().getString(R.string.no_network_information));
			}
		}

	}

