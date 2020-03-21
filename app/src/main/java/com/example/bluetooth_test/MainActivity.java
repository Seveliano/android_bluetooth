package com.example.bluetooth_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;

    private TextView tv_status;
    private Button btn_OnOFF, btn_search;
    private RecyclerView rel_devices;

    private Adapter_BT_Devices adapter_bt_devices;

    private BluetoothAdapter mBTAdapter;

    final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Model_Device model_device = new Model_Device(device.getName(), device.getAddress());
                adapter_bt_devices.add_device(model_device);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

            }
            else if (BluetoothDevice.ACTION_UUID.equals(action)){
                BluetoothDevice deviceExtra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                System.out.println("DeviceExtra address - " + deviceExtra.getAddress());
                if (uuidExtra != null) {
                    for (Parcelable p : uuidExtra) {
                        System.out.println("uuidExtra - " + p);
                    }
                } else {
                    System.out.println("uuidExtra is still null");
                }
//                if (!mDeviceList.isEmpty()) {
//                    BluetoothDevice device = mDeviceList.remove(0);
//                    boolean result = device.fetchUuidsWithSdp();
//                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_viewItem();
        init_bluetooth();
    }

    private void init_viewItem() {
        tv_status = findViewById(R.id.tv_status);
        btn_OnOFF = findViewById(R.id.btn_blue_OnOff);
        btn_search = findViewById(R.id.btn_search);

        rel_devices = findViewById(R.id.rel_devices);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rel_devices.setLayoutManager(llm);
        adapter_bt_devices = new Adapter_BT_Devices();
        rel_devices.setAdapter(adapter_bt_devices);

        btn_OnOFF.setOnClickListener(this);
        btn_search.setOnClickListener(this);
    }

    private void init_bluetooth(){
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver(mBTReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mBTReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(mBTReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));

        requestPermission();
    }

    @Override
    public void onClick(View item) {
        switch (item.getId()){
            case R.id.btn_blue_OnOff:
                bluetoothOnOff();
                break;
            case R.id.btn_search:
                searchDevices();
                break;
        }
    }

    private void bluetoothOnOff() {
        if (!mBTAdapter.isEnabled()){
            //== make blutooth on =====//
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
        else {
            //== make blutooth off =====//
            mBTAdapter.disable();
            tv_status.setText("Bluetooth Off");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                btn_OnOFF.setText("Bluetooth On");
                tv_status.setText("Bluetooth On");
            } else{
                btn_OnOFF.setText("Bluetooth Disabled");
                tv_status.setText("Bluetooth Disabled");
            }
        }
    }

    private void searchDevices() {
        if (mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            tv_status.setText("Searching stop");
            Toast.makeText(this, "Searching stop", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBTAdapter.isEnabled()){
                adapter_bt_devices.clear();
                mBTAdapter.startDiscovery();
                tv_status.setText("Searching...");
            }
            else {
                Toast.makeText(this,
                        "First turn on bluetooth", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestPermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBTReceiver);
    }
}
