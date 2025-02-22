/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.python.builtins.objects.getsetdescriptor;

import static com.oracle.graal.python.builtins.modules.io.IONodes.T__CHUNK_SIZE;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.J___NAME__;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.J___QUALNAME__;
import static com.oracle.graal.python.runtime.exception.PythonErrorType.AttributeError;
import static com.oracle.graal.python.runtime.exception.PythonErrorType.TypeError;
import static com.oracle.graal.python.util.PythonUtils.TS_ENCODING;

import java.util.List;

import com.oracle.graal.python.builtins.Builtin;
import com.oracle.graal.python.builtins.CoreFunctions;
import com.oracle.graal.python.builtins.PythonBuiltinClassType;
import com.oracle.graal.python.builtins.PythonBuiltins;
import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.PythonAbstractObject;
import com.oracle.graal.python.builtins.objects.str.StringUtils.SimpleTruffleStringFormatNode;
import com.oracle.graal.python.builtins.objects.type.TypeNodes.GetNameNode;
import com.oracle.graal.python.nodes.ErrorMessages;
import com.oracle.graal.python.nodes.HiddenAttr;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.attributes.GetAttributeNode.GetFixedAttributeNode;
import com.oracle.graal.python.nodes.call.special.CallBinaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallUnaryMethodNode;
import com.oracle.graal.python.nodes.classes.IsSubtypeNode;
import com.oracle.graal.python.nodes.function.PythonBuiltinBaseNode;
import com.oracle.graal.python.nodes.function.builtins.PythonUnaryBuiltinNode;
import com.oracle.graal.python.nodes.object.GetClassNode;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.strings.TruffleString;

/**
 * Built-in functions shared between {@link PythonBuiltinClassType#GetSetDescriptor} and
 * {@link PythonBuiltinClassType#MemberDescriptor}.
 */
@CoreFunctions(extendClasses = {PythonBuiltinClassType.GetSetDescriptor, PythonBuiltinClassType.MemberDescriptor})
public final class DescriptorBuiltins extends PythonBuiltins {
    @Override
    protected List<? extends NodeFactory<? extends PythonBuiltinBaseNode>> getNodeFactories() {
        return DescriptorBuiltinsFactory.getFactories();
    }

    @Builtin(name = J___QUALNAME__, minNumOfPositionalArgs = 1, isGetter = true)
    @GenerateNodeFactory
    public abstract static class QualnameNode extends PythonUnaryBuiltinNode {
        @Specialization
        static TruffleString doGetSetDescriptor(VirtualFrame frame, GetSetDescriptor self,
                        @Shared @Cached("create(T___QUALNAME__)") GetFixedAttributeNode readQualNameNode,
                        @Shared("formatter") @Cached SimpleTruffleStringFormatNode simpleTruffleStringFormatNode) {
            return simpleTruffleStringFormatNode.format("%s.%s", toStr(readQualNameNode.executeObject(frame, self.getType())), self.getName());
        }

        @Specialization
        static TruffleString doHiddenAttrDescriptor(VirtualFrame frame, HiddenAttrDescriptor self,
                        @Shared @Cached("create(T___QUALNAME__)") GetFixedAttributeNode readQualNameNode,
                        @Shared("formatter") @Cached SimpleTruffleStringFormatNode simpleTruffleStringFormatNode) {
            return simpleTruffleStringFormatNode.format("%s.%s", toStr(readQualNameNode.executeObject(frame, self.getType())), self.getAttr().getName());
        }

        @TruffleBoundary
        private static String toStr(Object o) {
            // TODO GR-37980
            return o.toString();
        }
    }

    @Builtin(name = J___NAME__, minNumOfPositionalArgs = 1, isGetter = true)
    @GenerateNodeFactory
    public abstract static class NameNode extends PythonUnaryBuiltinNode {
        @Specialization
        static TruffleString doGetSetDescriptor(GetSetDescriptor self) {
            return self.getName();
        }

        @Specialization
        static TruffleString doHiddenAttrDescriptor(HiddenAttrDescriptor self,
                        @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            return fromJavaStringNode.execute(self.getAttr().getName(), TS_ENCODING);
        }
    }

    @GenerateInline
    @GenerateCached(false)
    @GenerateUncached
    abstract static class DescriptorCheckNode extends Node {
        public abstract void execute(Node inliningTarget, Object descrType, Object nameObj, Object obj);

        // https://github.com/python/cpython/blob/e8b19656396381407ad91473af5da8b0d4346e88/Objects/descrobject.c#L70
        @Specialization
        static void check(Node inliningTarget, Object descrType, Object name, Object obj,
                        @Cached GetClassNode getClassNode,
                        @Cached(inline = false) IsSubtypeNode isSubtypeNode,
                        @Cached(inline = false) PRaiseNode raiseNode) {
            Object type = getClassNode.execute(inliningTarget, obj);
            if (!isSubtypeNode.execute(type, descrType)) {
                throw raiseNode.raise(TypeError, ErrorMessages.DESC_S_FOR_N_DOESNT_APPLY_TO_N, name, descrType, type);
            }
        }
    }

    @GenerateUncached
    @GenerateInline(value = false)
    public abstract static class DescrGetNode extends Node {
        public abstract Object execute(VirtualFrame frame, Object descr, Object obj);

        @Specialization
        Object doGetSetDescriptor(VirtualFrame frame, GetSetDescriptor descr, Object obj,
                        @Bind("this") Node inliningTarget,
                        @Exclusive @Cached PRaiseNode.Lazy raiseNode,
                        @Cached GetNameNode getNameNode,
                        @Cached CallUnaryMethodNode callNode) {
            if (descr.getGet() != null) {
                return callNode.executeObject(frame, descr.getGet(), obj);
            } else {
                throw raiseNode.get(inliningTarget).raise(AttributeError, ErrorMessages.ATTR_S_OF_S_IS_NOT_READABLE, descr.getName(), getNameNode.execute(inliningTarget, descr.getType()));
            }
        }

        @Specialization
        Object doHiddenAttrDescriptor(HiddenAttrDescriptor descr, PythonAbstractObject obj,
                        @Bind("this") Node inliningTarget,
                        @Exclusive @Cached PRaiseNode.Lazy raiseNode,
                        @Cached HiddenAttr.ReadNode readNode) {
            Object val = readNode.execute(inliningTarget, obj, descr.getAttr(), PNone.NO_VALUE);
            if (val != PNone.NO_VALUE) {
                return val;
            }
            throw raiseNode.get(inliningTarget).raise(AttributeError, ErrorMessages.OBJ_N_HAS_NO_ATTR_S, descr.getType(), descr.getAttr().getName());
        }
    }

    @GenerateInline(value = false)
    public abstract static class DescrSetNode extends Node {
        public abstract Object execute(VirtualFrame frame, Object descr, Object obj, Object value);

        @Specialization
        Object doGetSetDescriptor(VirtualFrame frame, GetSetDescriptor descr, Object obj, Object value,
                        @Bind("this") Node inliningTarget,
                        @Cached GetNameNode getNameNode,
                        @Cached PRaiseNode.Lazy raiseNode,
                        @Cached CallBinaryMethodNode callNode) {
            if (descr.getSet() != null) {
                return callNode.executeObject(frame, descr.getSet(), obj, value);
            } else {
                throw raiseNode.get(inliningTarget).raise(AttributeError, ErrorMessages.ATTR_S_OF_S_OBJ_IS_NOT_WRITABLE, descr.getName(), getNameNode.execute(inliningTarget, descr.getType()));
            }
        }

        @Specialization
        static Object doHiddenAttrDescriptor(HiddenAttrDescriptor descr, PythonAbstractObject obj, Object value,
                        @Bind("this") Node inliningTarget,
                        @Cached HiddenAttr.WriteNode writeNode) {
            writeNode.execute(inliningTarget, obj, descr.getAttr(), value);
            return true;
        }
    }

    @GenerateInline(value = false)
    public abstract static class DescrDeleteNode extends Node {
        public abstract Object execute(VirtualFrame frame, Object descr, Object obj);

        @Specialization
        Object doGetSetDescriptor(VirtualFrame frame, GetSetDescriptor descr, Object obj,
                        @Bind("this") Node inliningTarget,
                        @Exclusive @Cached PRaiseNode.Lazy raiseNode,
                        @Cached GetNameNode getNameNode,
                        @Cached CallBinaryMethodNode callNode,
                        @Cached InlinedBranchProfile branchProfile) {
            if (descr.allowsDelete()) {
                return callNode.executeObject(frame, descr.getSet(), obj, DescriptorDeleteMarker.INSTANCE);
            } else {
                branchProfile.enter(inliningTarget);
                if (descr.getSet() != null) {
                    if (descr.getName().equalsUncached(T__CHUNK_SIZE, TS_ENCODING)) {
                        // This is a special error message case. see
                        // Modules/_io/textio.c:textiowrapper_chunk_size_set
                        throw raiseNode.get(inliningTarget).raise(AttributeError, ErrorMessages.CANNOT_DELETE);
                    }
                    throw raiseNode.get(inliningTarget).raise(TypeError, ErrorMessages.CANNOT_DELETE_ATTRIBUTE, getNameNode.execute(inliningTarget, descr.getType()), descr.getName());
                } else {
                    throw raiseNode.get(inliningTarget).raise(AttributeError, ErrorMessages.ATTRIBUTE_S_OF_P_OBJECTS_IS_NOT_WRITABLE, descr.getName(), obj);
                }
            }
        }

        @Specialization
        Object doHiddenAttrDescriptor(HiddenAttrDescriptor descr, PythonAbstractObject obj,
                        @Bind("this") Node inliningTarget,
                        @Exclusive @Cached PRaiseNode.Lazy raiseNode,
                        @Cached HiddenAttr.WriteNode writeNode,
                        @Cached HiddenAttr.ReadNode readNode,
                        @Cached InlinedConditionProfile profile) {
            // PyMember_SetOne - Check if the attribute is set.
            if (profile.profile(inliningTarget, readNode.execute(inliningTarget, obj, descr.getAttr(), PNone.NO_VALUE) != PNone.NO_VALUE)) {
                writeNode.execute(inliningTarget, obj, descr.getAttr(), PNone.NO_VALUE);
                return PNone.NONE;
            }
            throw raiseNode.get(inliningTarget).raise(PythonBuiltinClassType.AttributeError, ErrorMessages.S, descr.getAttr().getName());
        }
    }
}
