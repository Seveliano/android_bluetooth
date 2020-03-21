package com.example.bluetooth_test;

public class Model_Device {

    public String st_btName;
    public String st_btID;

    public Model_Device(String st_btName, String st_btID) {
        this.st_btName = (st_btName == null)? "null" : st_btName;
        this.st_btID = st_btID;
    }
}
