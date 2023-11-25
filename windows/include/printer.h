#ifndef PRINTER_H_
#define PRINTER_H_

#include <map>
#include <memory>
#include <sstream>
#include <vector>

struct Printer
{
    const std::string name;
    const std::string model;
    const bool default;
    const bool available;

    Printer(std::string name,
            std::string model,
            bool default,
            bool available)
        : name(name),
          model(model),
          default(default),
          available(available) {}
};

class PrintManager
{
private:
    static HANDLE _hPrinter;

public:
    PrintManager(){};
    static std::vector<Printer> listPrinters();
    static BOOL pickPrinter(std::string pPrinterName);
    static BOOL printBytes(std::vector<uint8_t> data);
    static BOOL close();
    operator HANDLE() { return _hPrinter; }
};

#endif // PRINTER_H_
