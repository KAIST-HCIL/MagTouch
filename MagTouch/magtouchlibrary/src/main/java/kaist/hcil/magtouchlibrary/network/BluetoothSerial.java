package kaist.hcil.magtouchlibrary.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

import kaist.hcil.magtouchlibrary.Settings;

public class BluetoothSerial {
    /*
        This is client side code
     */
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private static final String targetAddress = Settings.BluetoothTargetAddress;
    private final int BUFFER_SIZE = 1;
    private static final UUID RFCOMM_UUID = UUID.fromString("00000003-0000-1000-8000-00805F9B34FB");
    private BluetoothCallback callback;
    private StreamRunnable streamer;
    private Handler asyncHandler;

    public BluetoothSerial(BluetoothAdapter bluetooth, BluetoothCallback callback)
    {
        if (bluetooth ==  null) { return; }
        if ( !bluetooth.isEnabled() ) {
            // BluetoothAdapter not enabled
            return;
        }
        mBluetoothAdapter = bluetooth;
        //blockingQueue = queue;
        this.callback = callback;
        asyncHandler = new Handler();
    }

    public void start() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for(BluetoothDevice device:pairedDevices) {
            if (targetAddress.equals(device.getAddress())) {
                targetDevice = device;
            }
        }
        if (targetDevice == null) {
            throw new RuntimeException("No Target Device");
        }
        mBluetoothDevice = targetDevice;
        streamer = new StreamRunnable();
        new Thread(streamer).start();
    }

    public void write(String msg)
    {
        streamer.write(msg);
    }

    public void writeAsync(final String msg)
    {
        asyncHandler.post(new Runnable() {
            @Override
            public void run() {
                write(msg);
            }
        });
    }

    public boolean isConnected()
    {
        if(streamer.getThreadState() == StreamRunnable.CON_STATE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isRunning()
    {
        if(streamer.getThreadState() == StreamRunnable.RUNNING_STATE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private class StreamRunnable implements Runnable {

        //private final BluetoothServerSocket mmServerSocket;
        private BluetoothSocket mmSocket;
        private static final int INIT_STATE = 100;
        private static final int WAIT_STATE = 200;
        private static final int RUNNING_STATE = 300;
        private static final int CON_STATE = 400;
        private int threadState;
        private BufferedWriter mmOutWriter;
        private BufferedInputStream mmInStream;

        public StreamRunnable() {
            // Do nothing
        }

        @Override
        public void run() {
            threadState = INIT_STATE;
            connect();
            threadState = CON_STATE;
            streamIn();
            disconnect();
        }

        public void write(String msg)
        {
            // should be called after run

            try {
                mmOutWriter.write(msg);
                mmOutWriter.flush();
            } catch (IOException e) {
                Log.e("FingMag", "write error", e);
            }
        }

        public int getThreadState()
        {
            return threadState;
        }

        private void connect() {
            BluetoothSocket tmp = null;
            try
            {
                tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(RFCOMM_UUID);
            }
            catch (IOException e)
            {
                Log.e("FingMag", "Failed to open socket", e);
                throw new RuntimeException("Failed to open socket");
            }
            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();
            try
            {
                mmSocket.connect();
            }
            catch (IOException e)
            {
                Log.e("FingMag", "Failed to connect socket", e);
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }

                throw new RuntimeException("Failed to connect socket");
            }
        }

        private void streamIn() {
            if( !mmSocket.isConnected()) {
                throw new RuntimeException("Socket is not connected");
            }
            try {
                mmInStream = new BufferedInputStream(mmSocket.getInputStream());
                mmOutWriter = new BufferedWriter(new OutputStreamWriter(mmSocket.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to get instream");
            }
            threadState = RUNNING_STATE;
            byte[] buffer = new byte[BUFFER_SIZE];
            while(true) {
                try {
                    mmInStream.read(buffer, 0, BUFFER_SIZE);
                    if(callback != null)
                    {
                        callback.dataArrived(buffer);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }


        private void disconnect() {
            if (mmSocket.isConnected()) {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e("TT","Socket close failed");
                }
            }

        }
    }
}
