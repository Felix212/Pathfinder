package org.tensorflow.lite.examples.detection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.pileproject.drivecommand.model.com.ICommunicator;
import org.tensorflow.lite.examples.detection.env.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class RobotNavigator implements ICommunicator {
    private static final Logger LOGGER = new Logger();
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public RobotNavigator( BluetoothDevice device) throws NullPointerException {
        if (device == null) {
            throw new NullPointerException("Device should not be null");
        }
        mDevice = device;
    }

    @Override
    public void open() throws IOException {
        // NOTE: an orthodox method
        // it depends on the device if this call fails or not
        // so, we do a redundancy check with the below reflection method
        mSocket = mDevice.createRfcommSocketToServiceRecord(SPP_UUID);
        try {
            mSocket.connect();
        } catch (IOException firstIOException) {
            LOGGER.d("Failed to connect with an orthodox method");
            try {
                // a redundancy check
                Method method = mDevice.getClass().getMethod("createRfcommSocket", int.class);
                mSocket = (BluetoothSocket) method.invoke(mDevice, 1);
                mSocket.connect();
            } catch (IOException secondIOException) {
                LOGGER.d("Failed to connect with a redundancy method");
                // unable to connect; close the socket and get out
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                    LOGGER.d("It seems unable to recover");
                }
                throw secondIOException;
            } catch (Exception exception) {
                exception.printStackTrace();
                LOGGER.d("This exception should not be occurred in release version");
            }
        }

        mOutputStream = mSocket.getOutputStream();
        mInputStream = mSocket.getInputStream();
    }

    @Override
    public void close() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                LOGGER.e("Failed to close connection", e);
            }
        }
        mSocket = null;
    }

    @Override
    public void write(byte[] request) throws RuntimeException {
        try {
            mOutputStream.write(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] read(int length) throws RuntimeException {
        byte[] buffer = new byte[length];
        int numBytes;
        try {
            numBytes = mInputStream.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] result = new byte[numBytes];
        System.arraycopy(buffer, 0, result, 0, numBytes);

        return result;
    }
}
