package com.ul.ims.apdu.interpreter.SessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.interpreter.Exceptions.OutOfSequenceException;
import com.ul.ims.apdu.interpreter.TransportLayer.TransportLayer;

import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleSessionLayer implements SessionLayer {
    private TransportLayer transportLayer;
    private SessionLayerDelegate delegate;

    Semaphore openRequestLock = new Semaphore(1);
    private Promise.Settlement<ResponseApdu> openRequest = null;

    public SimpleSessionLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.transportLayer.setDelegate(this);
    }

    @Override
    public void setDelegate(SessionLayerDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Sends bytes and responds back with a response apdu.
     * @param data
     * @return
     */
    public synchronized Promise<ResponseApdu> send(byte[] data) {
        if(!openRequestLock.tryAcquire()) {
            return Promise.reject(new OutOfSequenceException());
        }
        Promise<ResponseApdu> p = new Promise<>(settlement -> {
            try {
                openRequest = settlement;
                transportLayer.write(data);
            } catch (Exception e) {
                settlement.reject(e);
            }
        });
        p.always(() -> {
            openRequest = null;
            openRequestLock.release();
        });
        return p;
    }

    public synchronized void cancelOpenRequest() {
        if (openRequest != null) {
            openRequest.reject(new Exception("called cancel on open request"));
        }
        openRequest = null;
        openRequestLock.release();
    }

    //Called when we receive something after sending something.
    private void onReceiveResponse(byte[] data) {
        try {
            ResponseApdu result =  new ResponseApdu(new ByteArrayInputStreamExtension(data));
            openRequest.resolve(result);
        } catch (Exception e) {
            e.printStackTrace();
            openRequest.reject(e);
        }
    }

    @Override
    public synchronized void onReceive(byte[] data) throws IOException, InvalidApduException {
        if (openRequest != null) {
            onReceiveResponse(data);
            return;
        }

        byte[] responseBytes = null;
        try {
            //Parse what is received
            CommandApdu commandApdu = CommandApdu.fromBytes(data);
            //Build response with what is received
            ResponseApdu response = null;
            if(commandApdu instanceof SelectCommand) {
                response = this.delegate.receivedSelectRequest((SelectCommand) commandApdu);
            } else if(commandApdu instanceof ReadBinaryCommand) {
                response = this.delegate.receivedReadRequest((ReadBinaryCommand) commandApdu);
            }
            if(response != null) {
                responseBytes = response.toBytes().toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseApdu response = unknownErrorResponse();
            responseBytes = response.toBytes().toByteArray();
        }
        this.transportLayer.write(responseBytes);
    }

    private ResponseApdu unknownErrorResponse() {
        return new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);
    }
}
