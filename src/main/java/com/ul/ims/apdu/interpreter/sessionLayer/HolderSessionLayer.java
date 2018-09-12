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
public class HolderSessionLayer implements SessionLayer {
    private TransportLayer transportLayer;
    private SessionLayerDelegate delegate;

    public HolderSessionLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.transportLayer.setDelegate(this);
    }

    @Override
    public Promise<ResponseApdu> send(CommandApdu command) {
        return Promise.reject(new Exception("Following the APDU specification a holder cannot start a send"));
    }

    @Override
    public void setDelegate(SessionLayerDelegate delegate) {
        this.delegate = (SessionLayerDelegate) delegate;
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
}
