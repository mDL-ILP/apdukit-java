package com.ul.ims.apdu.interpreter.sessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.ResponseApdu;

public interface ReaderSessionLayer extends SessionLayer {
    Promise<ResponseApdu> send(CommandApdu command);
}
