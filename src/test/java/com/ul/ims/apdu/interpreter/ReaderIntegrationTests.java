package com.ul.ims.apdu.interpreter;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.apps.ExampleApp;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.Exceptions.OutOfSequenceException;
import com.ul.ims.apdu.interpreter.Exceptions.ResponseApduStatusCodeError;
import com.ul.ims.apdu.interpreter.Mocks.TestHolder;
import com.ul.ims.apdu.interpreter.Mocks.TestReader;
import com.ul.ims.apdu.interpreter.PresentationLayer.ApduProtocolPresentationLayer;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.ServerSessionLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.ClientSessionLayer;

import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class ReaderIntegrationTests {

    private TransportLayerSimulator holderTransportLayer;
    private TransportLayerSimulator readerTransportLayer;
    private SessionLayer holderSessionLayer;
    private SessionLayer readerSessionLayer;
    private PresentationLayer holderPresentationLayer;
    private PresentationLayer readerPresentationLayer;

    private TestHolder holder;
    private TestReader reader;

    @Before
    public void setup() {
        setupTransportLayers();
    }

    private void setupTransportLayers() {
        holderTransportLayer = new TransportLayerSimulator();
        readerTransportLayer = new TransportLayerSimulator();
        holderTransportLayer.connect(readerTransportLayer);
        readerTransportLayer.connect(holderTransportLayer);
        setupSessionLayers();
    }

    private void setupSessionLayers() {
        holderSessionLayer = new ServerSessionLayer(holderTransportLayer);
        readerSessionLayer = new ClientSessionLayer(readerTransportLayer);
        setupPresentationLayers();
    }

    private void setupPresentationLayers() {
        holderPresentationLayer = new ApduProtocolPresentationLayer(holderSessionLayer);
        readerPresentationLayer = new ApduProtocolPresentationLayer(readerSessionLayer);
        setupApplicationLayers();
    }

    private void setupApplicationLayers() {
        this.holder = new TestHolder(holderPresentationLayer);
        this.reader = new TestReader(readerPresentationLayer);
    }

    @Test(expected = IOException.class)
    public void testDisconnected() throws Throwable {
        readerTransportLayer.close();
        CommandApdu message = new SelectCommand().setFileID(ExampleApp.instance.ValidDF_NormalLength2).setFileControlInfo(FileControlInfo.NOFCIReturn);
        Promise p = this.readerSessionLayer.send(message);
        Assert.assertNull(p.getValue(1000));
    }

    @Test
    public void testWrongApplication() throws Throwable {
        //Change app id
        this.holder.setAppId(ExampleApp.instance.ValidDF_NormalLength2);
        Promise p = reader.getFile(ExampleApp.instance.ValidEF1);
        try {
            Assert.assertNull(p.getValue());
        }catch(ResponseApduStatusCodeError e) {
            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
        }
    }

    @Test(expected = OutOfSequenceException.class)
    public void testDoubleSendOutOfSequence() throws Throwable {
        CommandApdu message = new SelectCommand().setFileID(ExampleApp.instance.ValidDF_NormalLength2).setFileControlInfo(FileControlInfo.NOFCIReturn);
        readerSessionLayer.send(message);//line 1
        Promise p2 = readerSessionLayer.send(message).then(e -> {//line 2
            Assert.fail("You cannot send two messages while we have one open request standing");
            return null;
        });

        p2.getValue();
    }

    @Test(expected = ParseException.class)
    public void test_ReaderOnReceive_InvalidResponse() throws Throwable {
        //Mock transport layer so it doesn't actually write.
        this.readerTransportLayer = mock(TransportLayerSimulator.class);
        setupSessionLayers();
        this.reader = mock(TestReader.class);
        readerPresentationLayer.setDelegate(this.reader);

        //Do a select command.
        CommandApdu message = new SelectCommand().setFileID(ExampleApp.instance.ValidDF_NormalLength2).setFileControlInfo(FileControlInfo.NOFCIReturn);
        Promise p = readerSessionLayer.send(message).then(e -> {
            Assert.fail("Invalid response must not resolve promise.");
            return null;
        });
        //Then call the onReceive function with an invalid apdu.
        readerSessionLayer.onReceive(new byte[]{0, 0, 1});

        //Verify that the error was reported all the way back to the application
        verify(this.reader, timeout(100).times(1)).onReceiveInvalidApdu(isA(ParseException.class));
        p.getValue();//This will throw the expected exception. Because 0, 0, 1 isn't a valid response apdu.
    }

//    @Test(expected = InvalidApduException.class)
//    public void test_ReaderOnReceive_WithOpenRequest_InvalidResponse() throws Throwable {
//        this.transportLayerSimulatorReader = mock(TransportLayerSimulator.class);
//        this.sessionLayerReader = new ClientSessionLayer(transportLayerSimulatorReader);
//        this.sessionLayerReader.setDelegate(this.reader.readerPresentationLayer);
//
//        byte[] randomPayload = {0};
//        Promise p = sessionLayerReader.send(randomPayload).then(e -> {
//            Assert.fail("Invalid response must not resolve promise.");
//            return null;
//        });
//        sessionLayerReader.onReceive(randomPayload);
//        p.getValue();
//    }
//
//    @Test
//    public void test_getFile_ShortId_Success() throws Throwable {
//        byte[] expected = new byte[]{01, 02, 03, 07};
//        assertTrue("Could not set file", holder.holderPresentationLayer.setFile(ExampleApp.instance.ValidEF1, expected));
//
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.ValidEF1);
//        Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
//    }
//
//    @Test
//    public void test_getFile_ShortId_FileNotAvailable() throws Throwable {
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.ValidEF1).then((res) -> {
//            Assert.fail("File not set. Therefore, promise must fail.");
//            return null;
//        });
//        try {
//            p.getValue(1000);
//            Assert.fail("It should've thrown an exception");
//        }catch (ResponseApduStatusCodeError e) {
//            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
//        }
//    }
//
//    @Test
//    public void test_getFile_NormalId_Success() throws Throwable {
//        byte[] expected = new byte[]{01, 02, 05, 06};
//        assertTrue("Can't set file", holder.holderPresentationLayer.setFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId, expected));
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId);
//        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
//    }
//
//    @Test
//    public void test_getFile_NormalId_FileNotAvailable() throws Throwable {
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId).then((res) -> {
//            Assert.fail("File not set. Therefore, promise must fail.");
//            return null;
//        });
//        try {
//            p.getValue(1000);
//            Assert.fail("It should've thrown an exception");
//        }catch (ResponseApduStatusCodeError e) {
//            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
//        }
//    }
//
//    @Test
//    public void test_getFile_NormalId_LargeFile() throws Throwable {
//        byte[] expected = ExampleApp.instance.Datagroup1;
//        assertTrue("Can't set file", holder.holderPresentationLayer.setFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId, expected));
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId);
//        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
//    }
//
//    @Test
//    public void test_getFile_ShortId_LargeFile() throws Throwable {
//        byte[] expected = ExampleApp.instance.Datagroup1;
//        assertTrue("Can't set file", holder.holderPresentationLayer.setFile(ExampleApp.instance.ValidEF2, expected));
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.ValidEF2);
//        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
//    }
//
//    //Tests race condition with get file.
//    @Test
//    public void test_getFile_OutOfSequence() throws Throwable {
//        byte[] expected = new byte[]{01, 02, 03, 07};
//        assertTrue("Could not set file", holder.holderPresentationLayer.setFile(ExampleApp.instance.ValidEF1, expected));
//
//        Thread thread = new Thread(() -> {
//            Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.ValidEF1);
//            try {
//                Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
//            } catch (Throwable throwable) {
//                Assert.fail(throwable.getMessage());
//            }
//        });
//        thread.start();
//
//        Promise p = reader.readerPresentationLayer.getFile(ExampleApp.instance.ValidEF1);
//
//        Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
//        thread.join();
//    }

}
