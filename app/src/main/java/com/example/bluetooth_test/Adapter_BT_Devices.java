package com.example.bluetooth_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Adapter_BT_Devices extends RecyclerView.Adapter<Adapter_BT_Devices.MyViewHolder> {

    List<Model_Device> model_devices = new ArrayList<>();

    public void add_device(Model_Device model_device){
        model_devices.add(model_device);
        notifyDataSetChanged();
    }

    public void clear(){
        model_devices.clear();
        notifyDataSetChanged();
    }

    public Adapter_BT_Devices(){

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_device, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_deviceName.setText(model_devices.get(position).st_btName);
        holder.tv_deviceID.setText(model_devices.get(position).st_btID);
    }

    @Override
    public int getItemCount() {
        return model_devices.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView tv_deviceName, tv_deviceID;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            tv_deviceName = itemView.findViewById(R.id.tv_deviceName);
            tv_deviceID = itemView.findViewById(R.id.tv_deviceID);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String address = model_devices.get(getAdapterPosition()).st_btID;
                    final String name = model_devices.get(getAdapterPosition()).st_btName;

                    Intent intent = new Intent(itemView.getContext(), Activity_Connect.class);
                    intent.putExtra("name", name);
                    intent.putExtra("address", address);
                    itemView.getContext().startActivity(intent);


                }
            });
        }


    }


}
