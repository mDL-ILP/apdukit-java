<img src="./static/kit.png" alt="APDUKit logo" height="100" >

# Application Protocol Data Unit Kit
[![pipeline status](https://git.ul-ts.com/sl-advisory/RDW/apdukit-java/badges/develop/pipeline.svg)](https://git.ul-ts.com/sl-advisory/RDW/apdukit-java/commits/develop)

The apdukit-java is a Java library (based on the ISO 18013-5 standard) that aims to help developers encode (build and parse) and interpret APDUs. This documentation assumes that the reader is familiar with APDUs and possess a working knowledge of the ISO 7816-4 standard. More than as a usage guide, it serves to elucidate the structure of the code, for a reader familiar with the said standards. This library can be used in the context of mobile devices or smart cards.

## Mobile Driving License Interoperability Learning Platform

This project is a part of the mobile Driving License Interoperability Learning Platform. Please see the [main repository](https://github.com/mDL-ILP/mDL-ILP) for more information.

## Navigating through the source code

The source code is contained in the main package, while unit and integration tests are in the test package. This library may be wrapped in an implementation of choice tailored to usage for smartcards or mobile devices. An illustration of how such a wrapper might be created is documented in the test package. This package is further split into subdirectories encoding (which contains unit tests for all the supported commands and utilities) and interpreter (which contains integration tests for the abstraction layer over encoding). In the interpreter, The class IntegrationTests instantiates all the interpreter layers as required. The TestReader and TestHolder extend the abstract Reader and Holder Applications while the Reader and Holder IntegrationTests show how the library may be wrapped into an implementation of an mDL Reader/Holder.
The main package is further split into four packages:

### apps 

An example application with sample Dedicated File Ids, Elementary File Ids, and Elementary File Data. There are also some invalid FileIds (where validity is determined based on conformance to the ISO 18013-5 standard) defined in the example instance to test unhappy flows.

### encoding 

This package contains the objects to build and parse APDUs. The base interface Apdu.java is implemented by a ResponseApdu object and an abstract CommandApdu object (which in turn is extended by the various commands currently supported). Furthermore, the package contains four sub-directories:

#### enums

Contains various objects that (necessarily) form a part of various APDUs. These are needed to build and parse APDUs. These include FCITemplate, FileType (to distinguish between EFs and DFs during selection)

#### types

Contains objects that determine various types (of FileIds, Files, etc.). Both the objects ElementaryFileId and DedicatedFileId implement FileId. ApduFile is an object that is populated with actual data contained in any ElementaryFile or DataGroup. This is populated both when the holder of the data sets it locally, and the receiver of the data retrieves them remotely (from the holder or the Issuing Authority) to make a local instance of their own. The object TLVInfo is responsible for parsing the TLV structure within an ApduFile (for instance, when determining whether an ApduFile is complete).

#### utilities

Contains various utility methods relevant for building and parsing APDUs.

#### exceptions

Contains various exceptions that are thrown in unhappy flows in the process of building, or parsing.

### extensions

This package contains extensions to standard Java objects. These extensions (so far) have been written with the sole purpose of ensuring that the Java and Swift versions of the libraries are consistent with each other.

### interpreter

This package provides a layer of abstraction above the encoding package. Effort has been put into following the Open Systems Interconnection (OSI) model. In the conception of this software, it is assumed that devices being used for transportation of APDUs are supported by hardware that comes with the Physical, Data Link, and Network layers built in. Therefore, the interpreter contains:

#### transportLayer

Any transport layer of choice must implement the TransportLayer interface. This may be Bluetooth Low Energy, Wifi Aware, NFC, or any other transport channel that may be relevant. For the purposes of this software, a simulator that implements the interface has been written to test various flows supported by the software. 
This layer is responsible for writing data to a remote device, receiving data from a remote device, and delegating the received data to its delegate (which is mean to be a SessionLayer). On certain transport events, the layer is also responsible for bubbling up the occurrence of these events to its delegate. For instance, this may be an event to notify the layers above that the transport channel is ready for transport, or for closing down connection, etc.

#### sessionLayer

The SessionLayer interface extends the TransportLayerDelegate. This interface is independently extended by the Reader and Holder SessionLayer interafaces, which in turn are implemented by the corresponding Reader and Holder sessions (which must be instantiated with corresponding TransportLayers). Since APDUs are sequential where a response must be preceded by a command, this layer is held responsible for maintaining such sequential commands and responses. Moreover, since the issuer of commands never sends a ResponseApdu and the responder never issues a CommandApdu, their sessions are independently maintained.
The ReaderSession issues a send() in a synchronised fashion, thereby creating a new openRequest which is only released upon receiving a response to the command via the onReceive(). The send() calls on the write() method in the TransportLayer for writing to the remote holder. The onReceive() validates that the received data is indeed a ResponseApdu and calls on its own delegate for further handling.
The HolderSession on the other hand, decides if the received bytes (onReceive()) are recognised as known CommandApdus and requests its delegate for an appropriate response before calling the sendResponse() that writes to its TransportLayer.

#### presentationLayer

The PresentationLayer interface extends the SessionLayerDelegate, while the Reader and Holder PresentationLayer interfaces extend the generic PresentationLayer. These are correspondingly implemented by the Reader and Holder Presentations. The ReaderPresentation maintains the state of the selected Dedicated and Elementary Files. It sets and exposes the various selectDF, selectEF, and readBinary methods that are passed on to the SessionLayer; these methods return Promises that are to be fulfilled upon receiving responses. Furthermore, the ReaderPresentation reads responses to the issued Commands, fulfilling or rejecting the waiting promises, with the received response. The HolderPresentation on the other hand, manages the received select and ReadBinary commands, sets the Dedicated (AppId) and/or Elementary FileIds. After processing the received commands, the HolderPresentation builds an appropriate response after ensuring (with its delegate) that the appropriate FileIds are selected and retrieving the corresponding ApduFile data. The PresentationLayer also passes along relevant events to its delegate (the ApplicationLayer) for appropriate course of action.

#### applicationLayer

The ApplicationLayer interface extends the PresentationLayerDelegate, while the Reader and Holder Application Layers extend the ApplicationLayer interface, in addition to the Reader and Holder PresentationLayerDelegates, respectively. The Reader and Holder Applications implement these interfaces. The ReaderApplication exposes the readFile() method (which returns a Promise of file data) holding the logic for the order in which commands must be issued to read an ElementaryFile from the holder. It first selects a DedicatedFile, then issues a ReadBinaryShortFileId, if a short EFId is available. If not, it selects an ElementaryFile to then issue a ReadBinaryOffsetCommand. With the first chunk of data received from the holder, it opens an ApduFile, to then resolve the file. The resolution involves an issuing of ReadBinaryOffset commands with appropriate offsets until the file is complete. Upon receiving the complete file, the promise of the readFIle is fulfilled. In case of failure, the promise is rejected with the appropriate reason. The HolderApplication exposes the setLocalFile() method which sets the available ElementaryFiles with their corresponding FileIds in local key-value pairs. Upon receiving a request for the set files, the getLocalFile() method returns the ApduFile if it is available. Both the Reader and Holder Applications are abstract clases that may be extended inside the application that uses this library/software.

## How to extend the library

There are, of course, many more (Command) Apdus than are currently supported by this apdukit. Obvious examples are the InternalAuthenticate command, and other commands that are relevant for SecureMessaging. The steps that are necessary to extend this library with further commands is as follows:

1.	First, the classes corresponding to the commands must be created in the encoding package. 
2.	The SessionLayer must then be extended to handle the data passed into onReceive().
3.	The PresentationLayer must be extended to contain logic that dictates action upon receiving the new command.
4.	The ApplicationLayer must decide on which part of the flow the new commands must be issued, whether they are contained in the flow that is part of the existing readFile(), or if new methods must be exposed. 
5.	Integration tests for the new commands must be added to the test/interpreter package, whereas unit tests must be added to the test/encoding package.

## Some sequence diagrams

![Happy flow using the ReadBinaryShortFileId command](https://github.com/mDL-ILP/apdukit-java/APDU.png)

## Installation

### Gradle
```
buildscript {
  repositories {
    maven { url "https://jitpack.io" }
  }
}

dependencies {
  # for JVM-only projects
  implement com.github.mDL-ILP:apdukit-java:x.y.z
}
```

## Using in an Android project
1. Clone this project
2. Install Android Studio
