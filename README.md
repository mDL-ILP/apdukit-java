<img src="./static/kit.png" alt="APDUKit logo" height="100" >

# Application Protocol Data Unit Kit
[![pipeline status](https://git.ul-ts.com/sl-advisory/RDW/apdukit-java/badges/develop/pipeline.svg)](https://git.ul-ts.com/sl-advisory/RDW/apdukit-java/commits/develop)

This aims to help developers build, parse and interpret APDU. An application protocol data unit (APDU) is commonly used in the context of smart cards and is the communication unit between a smart card reader and a smart card. The structure of the APDU is defined by ISO/IEC 7816-4 which this project tries and partly encapsulate and simplify.

This can be used in the context of mobile devices or smart cards.

## Mobile Driving License Interoperability Learning Platform

This project is apart of the mobile Driving License Interoperability Learning Platform. Please see the [main repository](https://github.com/mDL-ILP/mDL-ILP) for more information.

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
