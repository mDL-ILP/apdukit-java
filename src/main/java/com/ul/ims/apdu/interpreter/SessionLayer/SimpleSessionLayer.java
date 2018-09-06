package com.ul.ims.apdu.interpreter.SessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.*;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.Exceptions.OutOfSequenceException;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayer;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class SimpleSessionLayer implements SessionLayer {
    private TransportLayer transportLayer;
    private SessionLayerDelegate delegate;

    //Apdu's
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

    /**
     * Called when we've received a response on a previously sent request (command).
     * @param response
     */
    private void onReceiveResponse(ResponseApdu response) {
        if(openRequest == null) {
            return;
        }
        openRequest.resolve(response);
    }

    /**
     * Called when we've received a request (command) apdu.
     * @param request the external request.
     * @return response back to the requester.
     */
    private ResponseApdu onReceiveCommand(CommandApdu request) {
        ResponseApdu response = null;
        if (request instanceof SelectCommand) {
            response = this.delegate.receivedSelectRequest((SelectCommand) request);
        } else if (request instanceof ReadBinaryCommand) {
            response = this.delegate.receivedReadRequest((ReadBinaryCommand) request);
        }
        return response;
    }

    /**
     *  TransportLayerDelegate method, called when we receive bytes from the transport layer.
     *  It will decode the apdu and depending on the apdu call the message handler method that might return a apdu message that has to be sent back as
     *  a response.
     * @param buf raw bytes received from the transport layer.
     */
    public synchronized void onReceive(byte[] buf) {
        //Understand what we've received and handle it
        ResponseApdu response = null;
        try {
            Apdu message = this.decodeApdu(buf);
            if(message instanceof CommandApdu) {
                response = this.onReceiveCommand((CommandApdu) message);
            }
            if (message instanceof ResponseApdu) {
                this.onReceiveResponse((ResponseApdu) message);
            }
        } catch (ParseException e) {
            this.delegate.onReceiveInvalidApdu(e);
            if(openRequest != null) {
                openRequest.reject(e);
                openRequest = null;
            }
            response = new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);
        }

        //Constraint: Holder doesn't send a message back
        if(openRequest == null) {
            return;
        }

        //Send it back
        try {
            if (response != null) {
                this.transportLayer.write(response.toBytes().toByteArray());
            }
        }catch (Exception e) {
            this.delegate.onSendFailure(e);
            try {
                response = new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);
                this.transportLayer.write(response.toBytes().toByteArray());
            } catch (Exception ignored) {}
        }
    }

    /**
     * Decodes apdu from bytes.
     * @param buf that represent a command or response apdu
     * @return a command or response apdu
     * @throws ParseException if it was a malformed apdu
     */
    private Apdu decodeApdu(byte[] buf) throws ParseException {
        //Did we have an open request? Then we received an response
        if (openRequest != null) {
            return ResponseApdu.fromBytes(buf);
        } else {
            return CommandApdu.fromBytes(buf);
        }
    }

}
