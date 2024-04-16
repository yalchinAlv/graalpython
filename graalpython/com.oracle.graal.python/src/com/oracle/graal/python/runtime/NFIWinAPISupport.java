package com.oracle.graal.python.runtime;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.runtime.PosixSupportLibrary.AcceptResult;
import com.oracle.graal.python.runtime.PosixSupportLibrary.AddrInfoCursor;
import com.oracle.graal.python.runtime.PosixSupportLibrary.Buffer;
import com.oracle.graal.python.runtime.PosixSupportLibrary.GetAddrInfoException;
import com.oracle.graal.python.runtime.PosixSupportLibrary.Inet4SockAddr;
import com.oracle.graal.python.runtime.PosixSupportLibrary.Inet6SockAddr;
import com.oracle.graal.python.runtime.PosixSupportLibrary.InvalidAddressException;
import com.oracle.graal.python.runtime.PosixSupportLibrary.InvalidUnixSocketPathException;
import com.oracle.graal.python.runtime.PosixSupportLibrary.OpenPtyResult;
import com.oracle.graal.python.runtime.PosixSupportLibrary.PosixException;
import com.oracle.graal.python.runtime.PosixSupportLibrary.PwdResult;
import com.oracle.graal.python.runtime.PosixSupportLibrary.RecvfromResult;
import com.oracle.graal.python.runtime.PosixSupportLibrary.SelectResult;
import com.oracle.graal.python.runtime.PosixSupportLibrary.Timeval;
import com.oracle.graal.python.runtime.PosixSupportLibrary.UniversalSockAddr;
import com.oracle.graal.python.runtime.PosixSupportLibrary.UnixSockAddr;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.strings.TruffleString;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@ExportLibrary(PosixSupportLibrary.class)
public final class NFIWinAPISupport extends PosixSupport {
    private static final TruffleLogger LOGGER = PythonLanguage.getLogger(NFIWinAPISupport.class);

    protected final PosixSupport nativePosixSupport;
    protected final PosixSupport emulatedPosixSupport;

    public NFIWinAPISupport(PosixSupport nativePosixSupport, PosixSupport emulatedPosixSupport) {
        LOGGER.log(Level.FINE, "Using NFIWinAPISupport");
        this.nativePosixSupport = nativePosixSupport;
        this.emulatedPosixSupport = emulatedPosixSupport;
    }

    @Override
    public void setEnv(Env env) {
        nativePosixSupport.setEnv(env);
        emulatedPosixSupport.setEnv(env);
    }


    @ExportMessage
    public TruffleString getBackend(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getBackend(emulatedPosixSupport);
    }

    @ExportMessage
    public TruffleString strerror(int errorCode, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.strerror(emulatedPosixSupport, errorCode);
    }

    @ExportMessage
    public long getpid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getpid(emulatedPosixSupport);
    }

    @ExportMessage
    public int umask(int mask, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.umask(emulatedPosixSupport, mask);
    }

    @ExportMessage
    public int openat(int dirFd, Object pathname, int flags, int mode, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.openat(emulatedPosixSupport, dirFd, pathname, flags, mode);
    }

    @ExportMessage
    public int close(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.close(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public Buffer read(int fd, long length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.read(emulatedPosixSupport, fd, length);
    }

    @ExportMessage
    public long write(int fd, Buffer data, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.write(emulatedPosixSupport, fd, data);
    }

    @ExportMessage
    public int dup(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.dup(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public int dup2(int fd, int fd2, boolean inheritable, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.dup2(emulatedPosixSupport, fd, fd2, inheritable);
    }

    @ExportMessage
    public boolean getInheritable(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getInheritable(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public void setInheritable(int fd, boolean inheritable, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.setInheritable(emulatedPosixSupport, fd, inheritable);
    }

    @ExportMessage
    public int[] pipe(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.pipe(emulatedPosixSupport);
    }

    @ExportMessage
    public SelectResult select(int[] readfds, int[] writefds, int[] errorfds, Timeval timeout, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.select(emulatedPosixSupport, readfds, writefds, errorfds, timeout);
    }

    @ExportMessage
    public long lseek(int fd, long offset, int how, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.lseek(emulatedPosixSupport, fd, offset, how);
    }

    @ExportMessage
    public void ftruncate(int fd, long length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.ftruncate(emulatedPosixSupport, fd, length);
    }

    @ExportMessage
    public void fsync(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fsync(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public void flock(int fd, int operation, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.flock(emulatedPosixSupport, fd, operation);
    }

    @ExportMessage
    public void fcntlLock(int fd, boolean blocking, int lockType, int whence, long start, long length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fcntlLock(emulatedPosixSupport, fd, blocking, lockType, whence, start, length);
    }

    @ExportMessage
    public boolean getBlocking(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getBlocking(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public void setBlocking(int fd, boolean blocking, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.setBlocking(emulatedPosixSupport, fd, blocking);
    }

    @ExportMessage
    public int[] getTerminalSize(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getTerminalSize(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public long[] fstatat(int dirFd, Object pathname, boolean followSymlinks, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.fstatat(emulatedPosixSupport, dirFd, pathname, followSymlinks);
    }

    @ExportMessage
    public long[] fstat(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.fstat(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public long[] statvfs(Object path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.statvfs(emulatedPosixSupport, path);
    }

    @ExportMessage
    public long[] fstatvfs(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.fstatvfs(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public Object[] uname(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.uname(emulatedPosixSupport);
    }

    @ExportMessage
    public void unlinkat(int dirFd, Object pathname, boolean rmdir, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.unlinkat(emulatedPosixSupport, dirFd, pathname, rmdir);
    }

    @ExportMessage
    public void linkat(int oldFdDir, Object oldPath, int newFdDir, Object newPath, int flags, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.linkat(emulatedPosixSupport, oldFdDir, oldPath, newFdDir, newPath, flags);
    }

    @ExportMessage
    public void symlinkat(Object target, int linkpathDirFd, Object linkpath, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.symlinkat(emulatedPosixSupport, target, linkpathDirFd, linkpath);
    }

    @ExportMessage
    public void mkdirat(int dirFd, Object pathname, int mode, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.mkdirat(emulatedPosixSupport, dirFd, pathname, mode);
    }

    @ExportMessage
    public Object getcwd(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        LOGGER.log(Level.FINE, "Using NFIWinAPISupport.getcwd");
        return emulatedLib.getcwd(emulatedPosixSupport);
    }

    @ExportMessage
    public void chdir(Object path, @CachedLibrary("this.nativePosixSupport") PosixSupportLibrary nativeLib) throws PosixException {
        nativeLib.chdir(nativePosixSupport, Buffer.wrap(utf8StringToBytes((String) path)));
    }

    @TruffleBoundary
    private static byte[] utf8StringToBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }


    @ExportMessage
    public void fchdir(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fchdir(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public boolean isatty(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.isatty(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public Object opendir(Object path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.opendir(emulatedPosixSupport, path);
    }

    @ExportMessage
    public Object fdopendir(int fd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.fdopendir(emulatedPosixSupport, fd);
    }

    @ExportMessage
    public void closedir(Object dirStream, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.closedir(emulatedPosixSupport, dirStream);
    }

    @ExportMessage
    public Object readdir(Object dirStream, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.readdir(emulatedPosixSupport, dirStream);
    }

    @ExportMessage
    public void rewinddir(Object dirStream, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        emulatedLib.rewinddir(emulatedPosixSupport, dirStream);
    }

    @ExportMessage
    public Object dirEntryGetName(Object dirEntry, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.dirEntryGetName(emulatedPosixSupport, dirEntry);
    }

    @ExportMessage
    public Object dirEntryGetPath(Object dirEntry, Object scandirPath, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.dirEntryGetPath(emulatedPosixSupport, dirEntry, scandirPath);
    }

    @ExportMessage
    public long dirEntryGetInode(Object dirEntry, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.dirEntryGetInode(emulatedPosixSupport, dirEntry);
    }

    @ExportMessage
    public int dirEntryGetType(Object dirEntry, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.dirEntryGetType(emulatedPosixSupport, dirEntry);
    }

    @ExportMessage
    public void utimensat(int dirFd, Object pathname, long[] timespec, boolean followSymlinks, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.utimensat(emulatedPosixSupport, dirFd, pathname, timespec, followSymlinks);
    }

    @ExportMessage
    public void futimens(int fd, long[] timespec, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.futimens(emulatedPosixSupport, fd, timespec);
    }

    @ExportMessage
    public void futimes(int fd, Timeval[] timeval, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.futimes(emulatedPosixSupport, fd, timeval);
    }

    @ExportMessage
    public void lutimes(Object filename, Timeval[] timeval, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.lutimes(emulatedPosixSupport, filename, timeval);
    }

    @ExportMessage
    public void utimes(Object filename, Timeval[] timeval, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.utimes(emulatedPosixSupport, filename, timeval);
    }

    @ExportMessage
    public void renameat(int oldDirFd, Object oldPath, int newDirFd, Object newPath, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.renameat(emulatedPosixSupport, oldDirFd, oldPath, newDirFd, newPath);
    }

    @ExportMessage
    public boolean faccessat(int dirFd, Object path, int mode, boolean effectiveIds, boolean followSymlinks, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.faccessat(emulatedPosixSupport, dirFd, path, mode, effectiveIds, followSymlinks);
    }

    @ExportMessage
    public void fchmodat(int dirFd, Object path, int mode, boolean followSymlinks, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fchmodat(emulatedPosixSupport, dirFd, path, mode, followSymlinks);
    }

    @ExportMessage
    public void fchmod(int fd, int mode, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fchmod(emulatedPosixSupport, fd, mode);
    }

    @ExportMessage
    public void fchownat(int dirFd, Object pathname, long owner, long group, boolean followSymlinks, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fchownat(emulatedPosixSupport, dirFd, pathname, owner, group, followSymlinks);
    }

    @ExportMessage
    public void fchown(int fd, long owner, long group, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.fchown(emulatedPosixSupport, fd, owner, group);
    }

    @ExportMessage
    public Object readlinkat(int dirFd, Object path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.readlinkat(emulatedPosixSupport, dirFd, path);
    }

    @ExportMessage
    public void kill(long pid, int signal, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.kill(emulatedPosixSupport, pid, signal);
    }

    @ExportMessage
    public void killpg(long pid, int signal, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.killpg(emulatedPosixSupport, pid, signal);
    }

    @ExportMessage
    public long[] waitpid(long pid, int options, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.waitpid(emulatedPosixSupport, pid, options);
    }

    @ExportMessage
    public void abort(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        emulatedLib.abort(emulatedPosixSupport);
    }

    @ExportMessage
    public boolean wcoredump(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wcoredump(emulatedPosixSupport, status);
    }

    @ExportMessage
    public boolean wifcontinued(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wifcontinued(emulatedPosixSupport, status);
    }

    @ExportMessage
    public boolean wifstopped(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wifstopped(emulatedPosixSupport, status);
    }

    @ExportMessage
    public boolean wifsignaled(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wifsignaled(emulatedPosixSupport, status);
    }

    @ExportMessage
    public boolean wifexited(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wifexited(emulatedPosixSupport, status);
    }

    @ExportMessage
    public int wexitstatus(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wexitstatus(emulatedPosixSupport, status);
    }

    @ExportMessage
    public int wtermsig(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wtermsig(emulatedPosixSupport, status);
    }

    @ExportMessage
    public int wstopsig(int status, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.wstopsig(emulatedPosixSupport, status);
    }

    @ExportMessage
    public long getuid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getuid(emulatedPosixSupport);
    }

    @ExportMessage
    public long geteuid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.geteuid(emulatedPosixSupport);
    }

    @ExportMessage
    public long getgid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getgid(emulatedPosixSupport);
    }

    @ExportMessage
    public long getegid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getegid(emulatedPosixSupport);
    }

    @ExportMessage
    public long getppid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getppid(emulatedPosixSupport);
    }

    @ExportMessage
    public long getpgid(long pid, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getpgid(emulatedPosixSupport, pid);
    }

    @ExportMessage
    public void setpgid(long pid, long pgid, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.setpgid(emulatedPosixSupport, pid, pgid);
    }

    @ExportMessage
    public long getpgrp(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getpgrp(emulatedPosixSupport);
    }

    @ExportMessage
    public long getsid(long pid, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getsid(emulatedPosixSupport, pid);
    }

    @ExportMessage
    public long setsid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.setsid(emulatedPosixSupport);
    }

    @ExportMessage
    public long[] getgroups(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getgroups(emulatedPosixSupport);
    }

    @ExportMessage
    public OpenPtyResult openpty(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.openpty(emulatedPosixSupport);
    }

    @ExportMessage
    public TruffleString ctermid(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.ctermid(emulatedPosixSupport);
    }

    @ExportMessage
    public void setenv(Object name, Object value, boolean overwrite, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.setenv(emulatedPosixSupport, name, value, overwrite);
    }

    @ExportMessage
    public void unsetenv(Object name, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.unsetenv(emulatedPosixSupport, name);
    }

    @ExportMessage
    public int forkExec(Object[] executables, Object[] args, Object cwd, Object[] env, int stdinReadFd, int stdinWriteFd, int stdoutReadFd, int stdoutWriteFd, int stderrReadFd, int stderrWriteFd, int errPipeReadFd, int errPipeWriteFd, boolean closeFds, boolean restoreSignals, boolean callSetsid, int[] fdsToKeep, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.forkExec(emulatedPosixSupport, executables, args, cwd, env, stdinReadFd, stdinWriteFd, stdoutReadFd, stdoutWriteFd, stderrReadFd, stderrWriteFd, errPipeReadFd, errPipeWriteFd, closeFds, restoreSignals, callSetsid, fdsToKeep);
    }

    @ExportMessage
    public void execv(Object pathname, Object[] args, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.execv(emulatedPosixSupport, pathname, args);
    }

    @ExportMessage
    public int system(Object command, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.system(emulatedPosixSupport, command);
    }

    @ExportMessage
    public Object mmap(long length, int prot, int flags, int fd, long offset, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.mmap(emulatedPosixSupport, length, prot, flags, fd, offset);
    }

    @ExportMessage
    public byte mmapReadByte(Object mmap, long index, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.mmapReadByte(emulatedPosixSupport, mmap, index);
    }

    @ExportMessage
    public void mmapWriteByte(Object mmap, long index, byte value, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.mmapWriteByte(emulatedPosixSupport, mmap, index, value);
    }

    @ExportMessage
    public int mmapReadBytes(Object mmap, long index, byte[] bytes, int length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.mmapReadBytes(emulatedPosixSupport, mmap, index, bytes, length);
    }

    @ExportMessage
    public void mmapWriteBytes(Object mmap, long index, byte[] bytes, int length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.mmapWriteBytes(emulatedPosixSupport, mmap, index, bytes, length);
    }

    @ExportMessage
    public void mmapFlush(Object mmap, long offset, long length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.mmapFlush(emulatedPosixSupport, mmap, offset, length);
    }

    @ExportMessage
    public void mmapUnmap(Object mmap, long length, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.mmapUnmap(emulatedPosixSupport, mmap, length);
    }

    @ExportMessage
    public long mmapGetPointer(Object mmap, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.mmapGetPointer(emulatedPosixSupport, mmap);
    }

    @ExportMessage
    public long semOpen(Object name, int openFlags, int mode, int value, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.semOpen(emulatedPosixSupport, name, openFlags, mode, value);
    }

    @ExportMessage
    public void semClose(long handle, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.semClose(emulatedPosixSupport, handle);
    }

    @ExportMessage
    public void semUnlink(Object name, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.semUnlink(emulatedPosixSupport, name);
    }

    @ExportMessage
    public int semGetValue(long handle, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.semGetValue(emulatedPosixSupport, handle);
    }

    @ExportMessage
    public void semPost(long handle, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.semPost(emulatedPosixSupport, handle);
    }

    @ExportMessage
    public void semWait(long handle, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.semWait(emulatedPosixSupport, handle);
    }

    @ExportMessage
    public boolean semTryWait(long handle, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.semTryWait(emulatedPosixSupport, handle);
    }

    @ExportMessage
    public boolean semTimedWait(long handle, long deadlineNs, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.semTimedWait(emulatedPosixSupport, handle, deadlineNs);
    }

    @ExportMessage
    public PwdResult getpwuid(long uid, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getpwuid(emulatedPosixSupport, uid);
    }

    @ExportMessage
    public PwdResult getpwnam(Object name, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getpwnam(emulatedPosixSupport, name);
    }

    @ExportMessage
    public boolean hasGetpwentries(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.hasGetpwentries(emulatedPosixSupport);
    }

    @ExportMessage
    public PwdResult[] getpwentries(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getpwentries(emulatedPosixSupport);
    }

    @ExportMessage
    public Object createPathFromString(TruffleString path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.createPathFromString(emulatedPosixSupport, path);
    }

    @ExportMessage
    public Object createPathFromBytes(byte[] path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.createPathFromBytes(emulatedPosixSupport, path);
    }

    @ExportMessage
    public TruffleString getPathAsString(Object path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getPathAsString(emulatedPosixSupport, path);
    }

    @ExportMessage
    public Buffer getPathAsBytes(Object path, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.getPathAsBytes(emulatedPosixSupport, path);
    }

    @ExportMessage
    public int socket(int domain, int type, int protocol, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.socket(emulatedPosixSupport, domain, type, protocol);
    }

    @ExportMessage
    public AcceptResult accept(int sockfd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.accept(emulatedPosixSupport, sockfd);
    }

    @ExportMessage
    public void bind(int sockfd, UniversalSockAddr addr, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.bind(emulatedPosixSupport, sockfd, addr);
    }

    @ExportMessage
    public void connect(int sockfd, UniversalSockAddr addr, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.connect(emulatedPosixSupport, sockfd, addr);
    }

    @ExportMessage
    public void listen(int sockfd, int backlog, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.listen(emulatedPosixSupport, sockfd, backlog);
    }

    @ExportMessage
    public UniversalSockAddr getpeername(int sockfd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getpeername(emulatedPosixSupport, sockfd);
    }

    @ExportMessage
    public UniversalSockAddr getsockname(int sockfd, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getsockname(emulatedPosixSupport, sockfd);
    }

    @ExportMessage
    public int send(int sockfd, byte[] buf, int offset, int len, int flags, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.send(emulatedPosixSupport, sockfd, buf, offset, len, flags);
    }

    @ExportMessage
    public int sendto(int sockfd, byte[] buf, int offset, int len, int flags, UniversalSockAddr destAddr, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.sendto(emulatedPosixSupport, sockfd, buf, offset, len, flags, destAddr);
    }

    @ExportMessage
    public int recv(int sockfd, byte[] buf, int offset, int len, int flags, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.recv(emulatedPosixSupport, sockfd, buf, offset, len, flags);
    }

    @ExportMessage
    public RecvfromResult recvfrom(int sockfd, byte[] buf, int offset, int len, int flags, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.recvfrom(emulatedPosixSupport, sockfd, buf, offset, len, flags);
    }

    @ExportMessage
    public void shutdown(int sockfd, int how, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.shutdown(emulatedPosixSupport, sockfd, how);
    }

    @ExportMessage
    public int getsockopt(int sockfd, int level, int optname, byte[] optval, int optlen, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.getsockopt(emulatedPosixSupport, sockfd, level, optname, optval, optlen);
    }

    @ExportMessage
    public void setsockopt(int sockfd, int level, int optname, byte[] optval, int optlen, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        emulatedLib.setsockopt(emulatedPosixSupport, sockfd, level, optname, optval, optlen);
    }

    @ExportMessage
    public int inet_addr(Object src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.inet_addr(emulatedPosixSupport, src);
    }

    @ExportMessage
    public int inet_aton(Object src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws InvalidAddressException {
        return emulatedLib.inet_aton(emulatedPosixSupport, src);
    }

    @ExportMessage
    public Object inet_ntoa(int address, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.inet_ntoa(emulatedPosixSupport, address);
    }

    @ExportMessage
    public byte[] inet_pton(int family, Object src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException, InvalidAddressException {
        return emulatedLib.inet_pton(emulatedPosixSupport, family, src);
    }

    @ExportMessage
    public Object inet_ntop(int family, byte[] src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.inet_ntop(emulatedPosixSupport, family, src);
    }

    @ExportMessage
    public Object gethostname(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.gethostname(emulatedPosixSupport);
    }

    @ExportMessage
    public Object[] getnameinfo(UniversalSockAddr addr, int flags, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws GetAddrInfoException {
        return emulatedLib.getnameinfo(emulatedPosixSupport, addr, flags);
    }

    @ExportMessage
    public AddrInfoCursor getaddrinfo(Object node, Object service, int family, int sockType, int protocol, int flags, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws GetAddrInfoException {
        return emulatedLib.getaddrinfo(emulatedPosixSupport, node, service, family, sockType, protocol, flags);
    }

    @ExportMessage
    public TruffleString crypt(TruffleString word, TruffleString salt, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws PosixException {
        return emulatedLib.crypt(emulatedPosixSupport, word, salt);
    }

    @ExportMessage
    public UniversalSockAddr createUniversalSockAddrInet4(Inet4SockAddr src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.createUniversalSockAddrInet4(emulatedPosixSupport, src);
    }

    @ExportMessage
    public UniversalSockAddr createUniversalSockAddrInet6(Inet6SockAddr src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.createUniversalSockAddrInet6(emulatedPosixSupport, src);
    }

    @ExportMessage
    public UniversalSockAddr createUniversalSockAddrUnix(UnixSockAddr src, @CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) throws InvalidUnixSocketPathException {
        return emulatedLib.createUniversalSockAddrUnix(emulatedPosixSupport, src);
    }

    @ExportMessage
    public boolean accepts(@CachedLibrary("this.emulatedPosixSupport") PosixSupportLibrary emulatedLib) {
        return emulatedLib.accepts(emulatedPosixSupport);
    }
}
