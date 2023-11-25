import 'dart:typed_data';

import 'package:enum_to_string/enum_to_string.dart';
import 'package:thermal_printer/printer.dart';
// import 'package:flutter_star_prnt/flutter_star_prnt.dart';

enum StarEmulation { StarPRNT, StarLine, StarGraphic }

class StarPrinter extends Printer {
  StarPrinter({StarEmulation emulation = StarEmulation.StarGraphic, int width = 580}) {
    this._emulation = EnumToString.convertToString(emulation);
    this._width = width;
  }

  // ignore: unused_field
  late final String _emulation;
  // ignore: unused_field
  late final int _width;
  late final String? _selectedPrinter;

  // static DiscoverResult<PortInfo> discoverStarPrinter() async {
  //   return (await StarPrnt.portDiscovery(StarPortType.All))
  //       .map((e) => PrinterDiscovered(
  //             name: e.modelName ?? 'Star Printer',
  //             detail: e,
  //           ))
  //       .toList();
  // }

  @override
  Future<bool> beep() async {
    return false;
  }

  @override
  Future<bool> image(Uint8List bytes, {int threshold = 150}) async {
    if (this._selectedPrinter == null) {
      throw new Exception("No printer available, please connect before sending.");
    }
    // final commands = PrintCommands();
    // commands.appendBitmapByte(byteData: bytes, width: this._width, diffusion: true, bothScale: true, alignment: StarAlignmentPosition.Center);
    // commands.appendCutPaper(StarCutPaperAction.PartialCutWithFeed);
    // final result = await StarPrnt.sendCommands(portName: this._selectedPrinter!, emulation: this._emulation, printCommands: commands);
    // return result.isSuccess;
    return true;
  }

  @override
  Future<bool> pulseDrawer() async {
    // final commands = PrintCommands();
    // commands.openCashDrawer(1);
    // commands.openCashDrawer(2);
    // final result = await StarPrnt.sendCommands(portName: this._selectedPrinter!, emulation: this._emulation, printCommands: commands);
    // return result.isSuccess;
    return true;
  }

  @override
  Future<bool> selfTest() async {
    return false;
  }

  @override
  Future<bool> setIp(String ipAddress) async {
    return false;
  }
}
