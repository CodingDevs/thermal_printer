// import 'package:flutter_star_prnt/flutter_star_prnt.dart';

import 'thermal_printer.dart';

class PrinterDiscovered<T> {
  String name;
  T detail;
  PrinterDiscovered({
    required this.name,
    required this.detail,
  });
}

typedef DiscoverResult<T> = Future<List<PrinterDiscovered<T>>>;
// typedef StarPrinterInfo = PortInfo;

// DiscoverResult<StarPrinterInfo> discoverStarPrinter() async {
//   if (Platform.isAndroid || Platform.isIOS) {
//     return (await StarPrnt.portDiscovery(StarPortType.All))
//         .map((e) => PrinterDiscovered<StarPrinterInfo>(
//               name: e.modelName ?? 'Star Printer',
//               detail: e,
//             ))
//         .toList();
//   }
//   return [];
// }

Future<List<PrinterDiscovered>> discoverPrinters(
    {List<DiscoverResult Function()> modes = const [
      // discoverStarPrinter,
      UsbPrinterConnector.discoverPrinters,
      BluetoothPrinterConnector.discoverPrinters,
      TcpPrinterConnector.discoverPrinters
    ]}) async {
  List<PrinterDiscovered> result = [];
  await Future.wait(modes.map((m) async {
    result.addAll(await m());
  }));
  return result;
}
