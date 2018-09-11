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
import com.ul.ims.apdu.interpreter.Mocks.TestReader;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class ReaderIntegrationTests extends IntegrationTests {

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
        Promise p = reader.getFile(ExampleApp.instance.ValidShortIdEF1);
        try {
            Assert.assertNull(p.getValue());
        }catch(ResponseApduStatusCodeError e) {
            Assert.assertEquals(StatusCode.ERROR_FILE_NOT_FOUND, e.getCode());
        }
    }

    @Test
    public void testGetFileWithShortId() throws Throwable {
        byte[] fileData = new byte[]{01, 02, 03, 07};
        //Set the file on the holder side.
        assertTrue("Holder could not set file", holder.setLocalFile(ExampleApp.instance.ValidShortIdEF1, fileData));

        Promise p = reader.getFile(ExampleApp.instance.ValidShortIdEF1);
        Assert.assertArrayEquals(fileData, (byte[])p.getValue(1000));
    }

    @Test
    public void testGetFileWithNormalId() throws Throwable {
        byte[] fileData = new byte[]{01, 02, 05, 06};
        assertTrue("Can't set file", holder.setLocalFile(ExampleApp.instance.ValidNormalIdEF, fileData));
        Promise p = reader.getFile(ExampleApp.instance.ValidNormalIdEF);
        Assert.assertArrayEquals(fileData, (byte[])p.getValue(100000));
    }

    @Test
    public void testGetFileUsingShortIdDoesNotExist() throws Throwable {
        Promise p = reader.getFile(ExampleApp.instance.ValidShortIdEF1).then((res) -> {
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
    public void testGetFileUsingNormalIdDoesNotExist() throws Throwable {
        Promise p = reader.getFile(ExampleApp.instance.ValidNormalIdEF).then((res) -> {
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
    public void testGetLargeFileUsingShortId() throws Throwable {
        byte[] expected = ExampleApp.instance.DatagroupE;
        assertTrue("Can't set file", holder.setLocalFile(ExampleApp.instance.ValidEF2, expected));
        Promise p = reader.getFile(ExampleApp.instance.ValidEF2);
        Assert.assertArrayEquals(expected, (byte[])p.getValue(100000));
    }

    @Test
    public void testGetLargeFileUsingNormalId() throws Throwable {
        byte[] expected = ExampleApp.instance.DatagroupE;//
        assertTrue("Can't set file", holder.setLocalFile(ExampleApp.instance.ValidNormalIdEF, expected));
        Promise p = reader.getFile(ExampleApp.instance.ValidNormalIdEF);
        Assert.assertArrayEquals("Expected equal our concatenated result", expected, (byte[])p.getValue(100000));
    }

    @Test(expected = OutOfSequenceException.class)
    public void testOutOfSequence() throws Throwable {
        CommandApdu message = new SelectCommand().setFileID(ExampleApp.instance.ValidDF_NormalLength2).setFileControlInfo(FileControlInfo.NOFCIReturn);
        readerSessionLayer.send(message);//Double send. Out of sequence!!
        Promise p2 = readerSessionLayer.send(message).then(e -> {//Double send. Out of sequence!!
            Assert.fail("You cannot send two messages while we have one open request standing");
            return null;
        });

        p2.getValue();
    }

    @Test(expected = ParseException.class)
    public void testInvalidOnReceive() throws Throwable {
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

    @Test
    public void testConcurrentGetFile() throws Throwable {
        byte[] expected = new byte[]{01, 02, 03, 07};
        assertTrue("Could not set file", holder.setLocalFile(ExampleApp.instance.ValidShortIdEF1, expected));

        //One thread asking for a file
        Thread thread = new Thread(() -> {
            Promise p = reader.getFile(ExampleApp.instance.ValidShortIdEF1);
            try {
                Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
            } catch (Throwable throwable) {
                Assert.fail(throwable.getMessage());
            }
        });
        thread.start();

        //Main thread asking for a file
        Promise p = reader.getFile(ExampleApp.instance.ValidShortIdEF1);

        Assert.assertArrayEquals(expected, (byte[])p.getValue(1000));
        thread.join();
    }

}
