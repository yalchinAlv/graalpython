/*
 * Copyright (c) 2020, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// Helper functions that mostly delegate to POSIX functions
// These functions are called from NFIPosixSupport Java class using NFI

// This file uses GNU extensions. Functions that require non-GNU versions (e.g. strerror_r)
// need to go to posix_no_gnu.c
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stddef.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>

int32_t wchdir(const wchar_t *path) {
    wchar_t path_buf[MAX_PATH], *new_path = path_buf;
    int result;
    wchar_t env[4] = L"=x:";

    if(!SetCurrentDirectoryW(path))
        return FALSE;
    result = GetCurrentDirectoryW(MAX_PATH, new_path);
    if (!result)
        return FALSE;
    if (result > MAX_PATH) {
        new_path = malloc(result * sizeof(wchar_t));
        if (!new_path) {
            SetLastError(ERROR_OUTOFMEMORY);
            return FALSE;
        }
        result = GetCurrentDirectoryW(result, new_path);
        if (!result) {
            free(new_path);
            return FALSE;
        }
    }
    int is_unc_like_path = (wcsncmp(new_path, L"\\\\", 2) == 0 ||
                            wcsncmp(new_path, L"//", 2) == 0);
    if (!is_unc_like_path) {
        env[1] = new_path[0];
        result = SetEnvironmentVariableW(env, new_path);
    }
     if (new_path != path_buf)
         free(new_path);
    return result ? TRUE : FALSE;
}

int32_t call_chdir(const char *path) {
    int length = MultiByteToWideChar(CP_UTF8, 0, path, -1, NULL, 0);
    if (length <= 0)
        return FALSE;

    wchar_t *wpath = malloc(length * sizeof(wchar_t));
    if (!wpath) {
        SetLastError(ERROR_OUTOFMEMORY);
        return FALSE;
    }

    int converted_length = MultiByteToWideChar(CP_UTF8, 0, path, -1, wpath, length);
    if (converted_length != length) {
        free(wpath);
        return FALSE;
    }

    int result = !wchdir(wpath);
    
    free(wpath);
    return result;
}