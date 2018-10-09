package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class HolderIntegrationTests extends IntegrationTests {

    @Test
    public void testInvalidOnReceive() throws Throwable {
        //Mock transport layer so it doesn't actually write.
        this.holderTransportLayer = mock(TransportLayerSimulator.class);
        setupSessionLayers();
        this.holder = mock(TestHolder.class);
        holderPresentation.setDelegate(this.holder);

        //Then call the onReceive function with an invalid apdu.
        holderSession.onReceive(new byte[]{0, 0, 1});

        //Verify that the error was reported all the way back to the application
        verify(this.holder, timeout(100).times(1)).onReceiveInvalidApdu(isA(ParseException.class));
        //Verify that it sent back a reply saying: Error unknown.
        verify(this.holderTransportLayer, timeout(100).times(1)).write(new byte[]{(byte) 0x6F, (byte) 0x00});
    }

}
