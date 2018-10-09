package com.ul.ims.apdu.interpreter.sessionLayer;

import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;

public interface HolderSessionLayerDelegate extends SessionLayerDelegate {
    // Responds with the binary data of that EF file.
    ResponseApdu receivedReadCommand(ReadBinaryCommand command);
    // Responds with the appropriate status code.
    ResponseApdu receivedSelectCommand(SelectCommand command);
    // Informs the delegate when got an exception when sending has failed.
}
