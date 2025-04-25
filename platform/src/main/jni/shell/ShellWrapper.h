#pragma once

#include <string>
#include <vector>
#include <map>
#include <memory>

class ShellWrapper {
public:
    ShellWrapper();

    ~ShellWrapper();

    [[nodiscard]] bool isAlive() const;

    void close();

    int exec(const std::vector<std::string> &command,
                    const std::function<void(const std::string &)> &onStdOut,
                    const std::function<void(const std::string &)> &onStdErr,
                    const std::map<std::string,
                            std::string> &env);

private:
    FILE *shell = nullptr;
    bool alive = false;
};
