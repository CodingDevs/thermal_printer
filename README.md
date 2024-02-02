# thermal_printer

[![Pub Version](https://img.shields.io/badge/pub-v1.0.5-green)](https://pub.dev/packages/thermal_printer)

A library to discover printers, and send printer commands.

This library allows to print esc commands to printers in different platforms such as android, ios, windows and different interfaces as Bluetooth and BLE, USB and Wifi/Ethernet

Inspired by [flutter_pos_printer](https://github.com/feedmepos/flutter_printer/tree/master/packages/flutter_pos_printer).


## Main Features
* Android, iOS and Windows support
* Scan for bluetooth devices
* Send raw `List<int> bytes` data to a device, review this library to generate ESC/POS commands [flutter_esc_pos_utils](https://pub.dev/packages/flutter_esc_pos_utils).

## Features

|                         |      Android       |         iOS          |      Windows       |            Description            |
| :---------------        | :----------------: | :------------------: | :----------------: | :-------------------------------- |
| USB interface           | :white_check_mark: |  :white_square_button: | :white_check_mark: | Allows connection with usb devices. |
| Bluetooth classic interface | :white_check_mark: |  :white_square_button:  | :white_square_button: | Allows connection with classic bt devices. |
| Bluetooth low energy (BLE) interface | :white_check_mark: |  :white_check_mark:  | :white_square_button: | Allows connection with bt BLE devices. |
| Net (ethernet/wifi) interface | :white_check_mark: |  :white_check_mark:  | :white_check_mark: | Allows connection with network devices. |
| scan                    | :white_check_mark: |  :white_check_mark:  | :white_check_mark: | Starts a scan for only Bluetooth devices or network devices(Android/iOS). |
| connect                 | :white_check_mark: |  :white_check_mark:  | :white_check_mark: | Establishes a connection to the device. |
| disconnect              | :white_check_mark: |  :white_check_mark:  | :white_check_mark: | Cancels an active or pending connection to the device. |
| state                   | :white_check_mark: |  :white_check_mark:  | :white_check_mark: | Stream of state changes for the Bluetooth Device. |
| print                   | :white_check_mark: |  :white_check_mark:  | :white_check_mark: | print bytes. |

## Getting Started

For a full example please check /example folder. Here are only the most important parts of the code to illustrate how to use the library.

Generate bytes to print through [flutter_esc_pos_utils](https://pub.dev/packages/flutter_esc_pos_utils).

```dart
    import 'package:esc_pos_utils/esc_pos_utils.dart';

    final profile = await CapabilityProfile.load();
    final generator = Generator(PaperSize.mm58, profile);
    List<int> bytes = [];

    bytes += generator.text('Test Print', styles: const PosStyles(align: PosAlign.center));
    bytes += generator.text('Product 1');
    bytes += generator.text('Product 2');
```

## Android
Allow to connect bluetooth (classic and BLE), USB and network devices

### Change the minSdkVersion for Android

thermal_printer is compatible only from version 21 of Android SDK so you should change this in android/app/build.gradle:

In build.gradle set
```
    defaultConfig {
        ...
        minSdkVersion 19
        targetSdkVersion 33
        ...
```

select type of device `PrinterType` ( bluetooth, usb, network)

if select bluetooth you can send optional params

- isBle -> allow to connect with bluetooth that supports this technology
- autoconnect -> allow to reconnect when state of device is None

USB: you can enable the native broadcast receiver to notify connected usb devices
put the following code in AndroidManifest
```
   <receiver
        android:name="com.codingdevs.thermal_printer.usb.UsbReceiver"
        android:exported="false">

        <intent-filter>
            <action android:name="android.hardware.usb.action.ACTION_USB_PERMISSION" />
            <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
            
        </intent-filter>
    </receiver>
```

## iOS
Allow to connect bluetooth (BLE) and network devices

## Windows
Allow to connect USB and network devices
To network devices is necessary to set ipAddress


## How to use it
### init a PrinterManager instance

```dart
import 'package:thermal_printer/thermal_printer.dart';

    var printerManager = PrinterManager.instance;

 ```

### scan

```dart
    var devices = [];
    _scan(PrinterType type, {bool isBle = false}) {
        // Find printers
        PrinterManager.instance.discovery(type: type, isBle: isBle).listen((device) {
            devices.add(device);
        });
    }
```

### connect

```dart
_connectDevice(PrinterDevice selectedPrinter, PrinterType type, {bool reconnect = false, bool isBle = false, String? ipAddress = null}) async {
    switch (type) {
      // only windows and android
      case PrinterType.usb:
        await PrinterManager.instance.connect(
            type: type,
            model: UsbPrinterInput(name: selectedPrinter.name, productId: selectedPrinter.productId, vendorId: selectedPrinter.vendorId));
        break;
      // only iOS and android
      case PrinterType.bluetooth:
        await PrinterManager.instance.connect(
            type: type,
            model: BluetoothPrinterInput(
                name: selectedPrinter.name,
                address: selectedPrinter.address!,
                isBle: isBle,
                autoConnect: reconnect));
        break;
      case PrinterType.network:
        await PrinterManager.instance.connect(type: type, model: TcpPrinterInput(ipAddress: ipAddress ?? selectedPrinter.address!));
        break;
      default:
    }
  }
```
### disconnect

```dart
    _disconnectDevice(PrinterType type) async {
        await PrinterManager.instance.disconnect(type: type);
        }
```

### listen bluetooth state
```dart
    PrinterManager.instance.stateBluetooth.listen((status) {
      log(' ----------------- status bt $status ------------------ ');
    });
```

### send bytes to print
```dart
    _sendBytesToPrint(List<int> bytes, PrinterType type) async { 
      PrinterManager.instance.send(type: type, bytes: bytes);
    }

```

## Troubleshooting

error:'State restoration of CBCentralManager is only allowed for applications that have specified the "bluetooth-central" background mode'
info.plist add:

```
<key>NSBluetoothAlwaysUsageDescription</key>
<string>Allow App use bluetooth?</string>
<key>NSBluetoothPeripheralUsageDescription</key>
<string>Allow App use bluetooth?</string>
<key>UIBackgroundModes</key>
<array>
    <string>bluetooth-central</string>
    <string>bluetooth-peripheral</string>
</array>
```


## Credits
- https://github.com/andrey-ushakov/esc_pos_utils
- https://github.com/bailabs/esc-pos-printer-flutter
- https://github.com/feedmepos/flutter_printer/tree/master/packages/flutter_pos_printer
