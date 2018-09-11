package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.Exceptions.ResponseApduStatusCodeError;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;

/**
 * The base APDU protocol presentation layer. It keeps state of what DF and EF are selected. Exposes methods to select DF or EF.
 */
abstract class BaseApduProtocolPresentationLayer implements PresentationLayer {
    protected PresentationLayerDelegate delegate;
    protected SessionLayer sessionLayer;

    //State
    protected DedicatedFileID selectedDF;
    protected ElementaryFileID selectedEF;

    public BaseApduProtocolPresentationLayer(SessionLayer sessionLayer) {
        this.sessionLayer = sessionLayer;
        this.sessionLayer.setDelegate(this);
    }

    public Promise selectDF(DedicatedFileID fileID) {
        SelectCommand command = new SelectCommand();
        command.setFileControlInfo(FileControlInfo.NOFCIReturn);
        command.setFileID(fileID);

        return Promise.resolve(selectedDF == fileID)
                .then(isAlreadySelected -> {
                    if (isAlreadySelected) {
                        return Promise.resolve(new ResponseApdu().setStatusCode(StatusCode.SUCCESSFUL_PROCESSING));
                    }
                    return this.sessionLayer.send(command);
                })
                .then(this::verifySelectResponse)
                .then(result -> {
                    selectedDF = fileID;
                    return Promise.resolve(result);
                });
    }

    public Promise selectEF(final ElementaryFileID fileID) {
        SelectCommand command = new SelectCommand();
        command.setFileControlInfo(FileControlInfo.NOFCIReturn);
        command.setFileID(fileID);

        return Promise.resolve(this.selectedEF == fileID)
                .then(isAlreadySelected -> {
                    if (isAlreadySelected) {
                        return Promise.resolve(new ResponseApdu().setStatusCode(StatusCode.SUCCESSFUL_PROCESSING));
                    }
                    return this.sessionLayer.send(command);
                })
                .then(this::verifySelectResponse)
                .then(result -> {
                    this.selectedEF = fileID;
                    return Promise.resolve(result);
                });
    }

    private Promise<ResponseApdu> verifySelectResponse(ResponseApdu response) {
        return new Promise<>(settlement -> {
            if (response.getStatusCode() == StatusCode.SUCCESSFUL_PROCESSING) {
                settlement.resolve(response);
            } else {
                settlement.reject(new ResponseApduStatusCodeError(response.getStatusCode()));
            }
        });
    }

    @Override
    public void setDelegate(PresentationLayerDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onSendFailure(Exception exception) {
        this.delegate.onSendFailure(exception);
    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        this.delegate.onReceiveInvalidApdu(exception);
    }
}
