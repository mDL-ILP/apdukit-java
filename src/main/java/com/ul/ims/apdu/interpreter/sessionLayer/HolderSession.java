package com.ul.ims.apdu.interpreter.sessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayer;

/**
 * The server session layer handles sending and receiving apdu messages. It decodes incoming bytes into Apdu objects and then calls the appropriate delegate message handle method.
 */
public class HolderSession implements HolderSessionLayer {
    private TransportLayer transportLayer;
    private HolderSessionLayerDelegate delegate;

    public HolderSession(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.transportLayer.setDelegate(this);
    }

    @Override
    public void setDelegate(SessionLayerDelegate delegate) {
        this.delegate = (HolderSessionLayerDelegate) delegate;
    }

    private void sendResponse(ResponseApdu response) {
        ResponseApdu reply = response;
        if(reply == null) {
            reply = new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);
        }
        try {
            this.transportLayer.write(reply.toBytes().toByteArray());
        } catch (Exception e) {
            this.delegate.onSendFailure(e);
            sendResponse(null);//Send unknown error back.
        }
    }

    @Override
    public void onReceive(byte[] data) {
        ResponseApdu response = null;
        try {
            CommandApdu request = CommandApdu.fromBytes(data);
            if (request instanceof SelectCommand) {
                response = this.delegate.receivedSelectCommand((SelectCommand) request);
            } else if (request instanceof ReadBinaryCommand) {
                response = this.delegate.receivedReadCommand((ReadBinaryCommand) request);
            }
        } catch (ParseException e) {
            this.delegate.onReceiveInvalidApdu(e);
        }
        sendResponse(response);
    }

    @Override
    public void onEvent(String string, int i) {
        this.delegate.onEvent(string, i);
    }
}
