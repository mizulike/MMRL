#include "ShellWrapper.h"
#include <cstdio>
#include <sstream>
#include <cstring>
#include <array>
#include <thread>
#include <vector>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>

ShellWrapper::ShellWrapper() {
    shell = popen("/system/bin/sh", "r+");
    alive = (shell != nullptr);
}

ShellWrapper::~ShellWrapper() {
    close();
}

bool ShellWrapper::isAlive() const {
    return alive;
}

void ShellWrapper::close() {
    if (shell) {
        pclose(shell);
        shell = nullptr;
        alive = false;
    }
}


int ShellWrapper::exec(const std::vector<std::string> &command,
                       const std::function<void(const std::string &)> &onStdOut,
                       const std::function<void(const std::string &)> &onStdErr,
                       const std::map<std::string, std::string> &env) {
    int stdoutPipe[2], stderrPipe[2];
    if (pipe(stdoutPipe) != 0 || pipe(stderrPipe) != 0) {
        onStdErr("Failed to create pipes.");
        return -1;
    }

    pid_t pid = fork();
    if (pid == -1) {
        onStdErr("Failed to fork.");
        return -1;
    }

    if (pid == 0) {
        // Child process
        dup2(stdoutPipe[1], STDOUT_FILENO);
        dup2(stderrPipe[1], STDERR_FILENO);
        ::close(stdoutPipe[0]);
        ::close(stderrPipe[0]);

        std::vector<const char *> args;
        args.reserve(command.size());
        for (const auto &arg: command) {
            args.push_back(arg.c_str());
        }
        args.push_back(nullptr);

        for (const auto &[key, val]: env) {
            setenv(key.c_str(), val.c_str(), 1);
        }

        execvp(args[0], const_cast<char *const *>(args.data()));
        _exit(127); // If exec fails
    }

    // Parent process
    ::close(stdoutPipe[1]);
    ::close(stderrPipe[1]);

    auto readPipe = [](int fd, const std::function<void(const std::string &)> &callback) {
        char buffer[256];
        std::string line;
        while (true) {
            ssize_t count = read(fd, buffer, sizeof(buffer) - 1);
            if (count <= 0) break;
            buffer[count] = '\0';
            std::istringstream stream(buffer);
            std::string lineBuf;
            while (std::getline(stream, lineBuf)) {
                lineBuf.erase(std::remove(lineBuf.begin(), lineBuf.end(), '\n'), lineBuf.end());
                callback(lineBuf);
            }
        }
    };

    std::thread tOut(readPipe, stdoutPipe[0], onStdOut);
    std::thread tErr(readPipe, stderrPipe[0], onStdErr);

    tOut.join();
    tErr.join();

    int status = 0;
    waitpid(pid, &status, 0);

    return WIFEXITED(status) ? WEXITSTATUS(status) : -1;
}