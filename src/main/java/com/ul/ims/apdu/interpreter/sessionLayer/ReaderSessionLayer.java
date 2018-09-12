package com.ul.ims.apdu.interpreter.sessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.exceptions.OutOfSequenceException;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayer;

import java.util.concurrent.Semaphore;

/**
 * The client session layer handles sending and receiving APDU messages. It also allows for sending APDU commands and keeping track of this open request. Then fulfilling the promise upon receiving data.
 */
public class ReaderSessionLayer implements SessionLayer {
    private TransportLayer transportLayer;
    private SessionLayerDelegate delegate;

    private Semaphore openRequestLock = new Semaphore(1);
    private Promise.Settlement<ResponseApdu> openRequest = null;

    public ReaderSessionLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.transportLayer.setDelegate(this);
    }

    private Promise<byte[]> commandToBytes(CommandApdu input) {
        return new Promise<>(settlement -> {
            try {
                settlement.resolve(input.toBytes().toByteArray());
            } catch (Exception e) {
                settlement.reject(e);
            }
        });
    }

    @Override
    public Promise<ResponseApdu> send(CommandApdu command) {
        if(!openRequestLock.tryAcquire()) {//Only one command at a time.
            return Promise.reject(new OutOfSequenceException());
        }
        Promise<ResponseApdu> p = commandToBytes(command).then(this::sendBytes);
        p.always(() -> {
            openRequest = null;
            openRequestLock.release();
        });
        return p;
    }

    private synchronized Promise<ResponseApdu> sendBytes(byte[] data) {
        return new Promise<>(settlement -> {
            try {
                openRequest = settlement;
                transportLayer.write(data);
            } catch (Exception e) {
                settlement.reject(e);
            }
        });
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

    @Override
    public void onEvent(String string, int i) {

    }
}
