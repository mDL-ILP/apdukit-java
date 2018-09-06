package com.ul.ims.apdu.interpreter;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.FileID;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.apps.ExampleApp;
import com.ul.ims.apdu.interpreter.Exceptions.OutOfSequenceException;
import com.ul.ims.apdu.interpreter.Exceptions.ResponseApduStatusCodeError;
import com.ul.ims.apdu.interpreter.PresentationLayer.SimpleApduPresentationLayer;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayerDelegate;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.SimpleSessionLayer;

import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class InterpreterIntegrationTests {

    private Holder holder;
    private Reader reader;

    private TransportLayerSimulator transportLayerSimulatorHolder;
    private TransportLayerSimulator transportLayerSimulatorReader;

    private SessionLayer sessionLayerHolder;
    private SessionLayer sessionLayerReader;

    class Holder implements PresentationLayerDelegate {
        PresentationLayer presentationLayer;

        Holder(PresentationLayer presentationLayer) {
            this.presentationLayer = presentationLayer;
            this.presentationLayer.setDelegate(this);
        }

        @Override
        public boolean checkAccessConditions(FileID id) {
            return true;
        }
    }

    class Reader implements PresentationLayerDelegate {
        PresentationLayer presentationLayer;

        Reader(PresentationLayer presentationLayer) {
            this.presentationLayer = presentationLayer;
            this.presentationLayer.setDelegate(this);
        }

        @Override
        public boolean checkAccessConditions(FileID id) {
            return false;
        }
    }

    @Before
    public void runBeforeTestMethod() {
        this.transportLayerSimulatorHolder = new TransportLayerSimulator();
        this.transportLayerSimulatorReader = new TransportLayerSimulator();
        transportLayerSimulatorHolder.connect(transportLayerSimulatorReader);
        transportLayerSimulatorReader.connect(transportLayerSimulatorHolder);

        this.sessionLayerHolder = new SimpleSessionLayer(transportLayerSimulatorHolder);
        this.sessionLayerReader = new SimpleSessionLayer(transportLayerSimulatorReader);

        PresentationLayer presentationLayerHolder = new SimpleApduPresentationLayer(sessionLayerHolder, ExampleApp.instance.ValidDF_NormalLength1);
        PresentationLayer presentationLayerReader = new SimpleApduPresentationLayer(sessionLayerReader, ExampleApp.instance.ValidDF_NormalLength1);

        this.holder = new Holder(presentationLayerHolder);
        this.reader = new Reader(presentationLayerReader);
    }

    @Test(expected = IOException.class)
    public void sendCommandRejectsPromise_WhenNoExternalTransportLayerIsConnected() throws Throwable {
        transportLayerSimulatorReader.connect(null);
        Promise p = this.sessionLayerReader.send(new byte[]{1, 2});
        Assert.assertNull(p.getValue(1000));
    }

    @Test
    public void holderDoesNotHaveFile_ForInvalidDF() throws Throwable {
        this.holder.presentationLayer = new SimpleApduPresentationLayer(sessionLayerHolder, ExampleApp.instance.ValidDF_NormalLength2);
        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.ValidEF1);
        try {
            Assert.assertNull(p.getValue());
        }catch(ResponseApduStatusCodeError e) {
            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
        }
    }

    @Test
    public void holderRespondsWithUnknownError6F00() throws Throwable {
        this.transportLayerSimulatorHolder = mock(TransportLayerSimulator.class);
        this.sessionLayerHolder = new SimpleSessionLayer(transportLayerSimulatorHolder);

        byte[] onReceiveData = new byte[] {(byte) 0x01, (byte) 0x02};
        this.sessionLayerHolder.onReceive(onReceiveData);

        verify(transportLayerSimulatorHolder, timeout(100).times(1)).write(new byte[]{(byte) 0x6F, (byte) 0x00});
    }

    @Test(expected = OutOfSequenceException.class)
    public void sequentialSendSELECT_BeforeOnReceive_ThrowsException() throws Throwable {
        this.transportLayerSimulatorReader = mock(TransportLayerSimulator.class);
        this.sessionLayerReader = new SimpleSessionLayer(transportLayerSimulatorReader);

        byte[] randomPayload = {0,1,2,3};
        sessionLayerReader.send(randomPayload);//line 1
        Promise p2 = sessionLayerReader.send(randomPayload).then(e -> {//line 2
            Assert.fail("You cannot send two messages while we have one open request standing");
            return null;
        });

        p2.getValue();
    }

    //Tests race condition with sending
    @Test(expected = InvalidApduException.class)
    public void sendSELECT_FollowedByOnReceiveInvalidResponseApdu1_ThrowsException() throws Throwable {
        this.transportLayerSimulatorReader = mock(TransportLayerSimulator.class);
        this.sessionLayerReader = new SimpleSessionLayer(transportLayerSimulatorReader);

        byte[] randomPayload = {0, 1, 2, 3};
        Promise p = sessionLayerReader.send(randomPayload).then(e -> {
            Assert.fail("Invalid response must not resolve promise.");
            return null;
        });
        sessionLayerReader.onReceive(randomPayload);
        p.getValue(1000);
    }

    //tests timeout
//    @Test(expected = TimeoutException.class)
//    public void testTimeout() throws Throwable {
//        this.transportLayerSimulatorReader = mock(TransportLayerSimulator.class);
//        this.sessionLayerReader = new SimpleSessionLayer(transportLayerSimulatorReader);
//
//        byte[] randomPayload = {0, 1, 2, 4};
//        Promise p = sessionLayerReader.send(randomPayload).then(e -> {
//            Assert.fail("Invalid response must not resolve promise.");
//            return null;
//        });
//        p.getValue(1000);
//    }

    @Test(expected = InvalidApduException.class)
    public void sendSELECT_FollowedByOnReceiveInvalidResponseApdu2_ThrowsException() throws Throwable {
        this.transportLayerSimulatorReader = mock(TransportLayerSimulator.class);
        this.sessionLayerReader = new SimpleSessionLayer(transportLayerSimulatorReader);

        byte[] randomPayload = {0};
        Promise p = sessionLayerReader.send(randomPayload).then(e -> {
            Assert.fail("Invalid response must not resolve promise.");
            return null;
        });
        sessionLayerReader.onReceive(randomPayload);
        p.getValue();
    }

    @Test
    public void holderRespondsWithRequestedFileUsingReadBinaryShortId() throws Throwable {
        byte[] expected = new byte[]{01, 02, 03, 07};
        assertTrue("Could not set file", holder.presentationLayer.setFile(ExampleApp.instance.ValidEF1, expected));

        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.ValidEF1);
        Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
    }

    @Test
    public void holderDoesNotHavetRequestedFile_UnsetReadBinaryShortId() throws Throwable {
        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.ValidEF1).then((res) -> {
            Assert.fail("File not set. Therefore, promise must fail.");
            return null;
        });
        try {
            p.getValue(1000);
            Assert.fail("It should've thrown an exception");
        }catch (ResponseApduStatusCodeError e) {
            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
        }
    }

    @Test
    public void holderRespondsWithRequestedFileUsingReadBinaryOffset() throws Throwable {
        byte[] expected = new byte[]{01, 02, 05, 06};
        assertTrue("Can't set file", holder.presentationLayer.setFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId, expected));
        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId);
        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
    }

    @Test
    public void holderDoesNotHavetRequestedFile_UnsetReadBinaryOffset() throws Throwable {
        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId).then((res) -> {
            Assert.fail("File not set. Therefore, promise must fail.");
            return null;
        });
        try {
            p.getValue(1000);
            Assert.fail("It should've thrown an exception");
        }catch (ResponseApduStatusCodeError e) {
            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
        }
    }

    @Test
    public void holderGetLargeFileUsingNormalFileIdFromReader() throws Throwable {
        byte[] expected = ExampleApp.instance.Datagroup1;
        assertTrue("Can't set file", holder.presentationLayer.setFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId, expected));
        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.InvalidShortFileId_ButValidNormalId);
        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
    }

    @Test
    public void holderGetLargeFileUsingShortFileIdFromReader() throws Throwable {
        byte[] expected = ExampleApp.instance.Datagroup1;
        assertTrue("Can't set file", holder.presentationLayer.setFile(ExampleApp.instance.ValidEF2, expected));
        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.ValidEF2);
        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
    }

    //Tests race condition with get file.
    @Test
    public void holderSequentialGetFile() throws Throwable {
        byte[] expected = new byte[]{01, 02, 03, 07};
        assertTrue("Could not set file", holder.presentationLayer.setFile(ExampleApp.instance.ValidEF1, expected));

        Thread thread = new Thread(() -> {
            Promise p = reader.presentationLayer.getFile(ExampleApp.instance.ValidEF1);
            try {
                Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
            } catch (Throwable throwable) {
                Assert.fail(throwable.getMessage());
            }
        });
        thread.start();

        Promise p = reader.presentationLayer.getFile(ExampleApp.instance.ValidEF1);

        Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
        thread.join();
    }

}
