package com.ul.ims.apdu.interpreter.SessionLayer;

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
public class ServerSessionLayer implements SessionLayer {
    private TransportLayer transportLayer;
    private SessionLayerDelegate delegate;

    public ServerSessionLayer(TransportLayer transportLayer) {
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
        try {
            this.transportLayer.write(response.toBytes().toByteArray());
        } catch (Exception e) {
            this.delegate.onSendFailure(e);
            try {
                response = new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);
                this.transportLayer.write(response.toBytes().toByteArray());
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onReceive(byte[] data) {
        ResponseApdu response = null;
        try {
            CommandApdu request = CommandApdu.fromBytes(data);
            if (request instanceof SelectCommand) {
                response = this.delegate.receivedSelectRequest((SelectCommand) request);
            } else if (request instanceof ReadBinaryCommand) {
                response = this.delegate.receivedReadRequest((ReadBinaryCommand) request);
            }
        } catch (ParseException e) {
            this.delegate.onReceiveInvalidApdu(e);
        }
        if(response == null) {
            response = new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);
        }
        sendResponse(response);
    }
}
