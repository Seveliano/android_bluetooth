package com.example.bluetooth_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Activity_Connect extends AppCompatActivity {

    private String address, name;

    private TextView tv_result;
    private Button btn_send;

    BluetoothSocket mBTSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BTConnecThread btConnecThread;

    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private Handler mHandler;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        tv_result = findViewById(R.id.tv_result);
        btn_send = findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBTSocket != null && btConnecThread != null){
                    btConnecThread.write("1");
                } else {
                    Toast.makeText(Activity_Connect.this, "Device is not connected!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //=== show view ============//
                    tv_result.setText(readMessage);
                }
                if (msg.what == CONNECTING_STATUS){
                    if (msg.arg1 == 1){
                        Toast.makeText(Activity_Connect.this,
                                "Connected to Device: " + msg.obj, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Activity_Connect.this,
                                "Connection Failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        connect_device();
    }

    private void connect_device(){

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Toast.makeText(this, "Turn on bluetooth and try again.",
                    Toast.LENGTH_LONG).show();
            this.finish();
        }

        progressDialog = ProgressDialog.show(Activity_Connect.this,
                "Connecting...", "Please Wait!!!");
        new Thread(){
            public void run() {
                boolean fail = false;

                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
                        .getRemoteDevice(address);
//                UUID uuid = device.getUuids()[0].getUuid();

                try {
                    mBTSocket = createBluetoothSocket(device);
                    if (BluetoothAdapter.getDefaultAdapter().isDiscovering()){
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    }
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(Activity_Connect.this, "Connect Failed", Toast.LENGTH_LONG).show();
                }

                try {

                    mBTSocket.connect();

                } catch (IOException e) {
                    fail = true;
                    try {
                        mBTSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                            .sendToTarget();
                }

                if (!fail) {
                    btConnecThread = new BTConnecThread(mBTSocket);
                    btConnecThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
//        try {
//            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
//            return (BluetoothSocket) m.invoke(device, myUUID);
//        } catch (Exception e) {
//            Log.e("TAG", "Could not create Insecure RFComm Connection",e);
//        }
//        return  device.createRfcommSocketToServiceRecord(myUUID);
//        final Method createMethod;
//        try {
//            createMethod = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
//            return (BluetoothSocket)createMethod.invoke(device, 1);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }

        return  device.createInsecureRfcommSocketToServiceRecord(myUUID);
    }

    public class BTConnecThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public BTConnecThread(BluetoothSocket socket){
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes = mInStream.available();
                    if (bytes != 0){
                        SystemClock.sleep(100);
                        bytes = mInStream.read(buffer, 0, bytes);
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();

            try {
                mOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
