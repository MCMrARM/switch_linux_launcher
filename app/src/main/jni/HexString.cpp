#include "HexString.h"
#include <sstream>
#include <iomanip>

std::string HexString::encode(char* data, size_t len) {
    std::stringstream ss;
    for (size_t i = 0; i < len; i++)
        ss << std::hex << std::setfill('0') << std::setw(2) << (int) (unsigned char) data[i];
    return ss.str();
}