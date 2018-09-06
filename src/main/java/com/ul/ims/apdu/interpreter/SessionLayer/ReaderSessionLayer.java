package com.ul.ims.apdu.interpreter.SessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.Exceptions.OutOfSequenceException;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayer;

import java.util.concurrent.Semaphore;

public class ReaderSessionLayer implements SessionLayer {
    private TransportLayer transportLayer;
    private SessionLayerDelegate delegate;

    //Apdu's
    Semaphore openRequestLock = new Semaphore(1);
    private Promise.Settlement<ResponseApdu> openRequest = null;

    public ReaderSessionLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.transportLayer.setDelegate(this);
    }

    @Override
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

    @Override
    public void setDelegate(SessionLayerDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onReceive(byte[] data) {
        //We received an unwanted response?
        if(this.openRequest == null) {
            this.delegate.onReceiveInvalidApdu(new InvalidApduException("received unwanted response"));
            return;
        }
        try {
            ResponseApdu response = ResponseApdu.fromBytes(data);
            this.openRequest.resolve(response);
        } catch (ParseException e) {
            this.openRequest.reject(e);
            this.delegate.onReceiveInvalidApdu(e);
        }

    }
}
