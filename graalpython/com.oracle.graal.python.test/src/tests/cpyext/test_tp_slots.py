# Copyright (c) 2024, 2024, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# The Universal Permissive License (UPL), Version 1.0
#
# Subject to the condition set forth below, permission is hereby granted to any
# person obtaining a copy of this software, associated documentation and/or
# data (collectively the "Software"), free of charge and under any and all
# copyright rights in the Software, and any and all patent rights owned or
# freely licensable by each licensor hereunder covering either (i) the
# unmodified Software as contributed to or provided by such licensor, or (ii)
# the Larger Works (as defined below), to deal in both
#
# (a) the Software, and
#
# (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
# one is included with the Software each a "Larger Work" to which the Software
# is contributed by such licensors),
#
# without restriction, including without limitation the rights to copy, create
# derivative works of, display, perform, and distribute the Software and make,
# use, sell, offer for sale, import, export, have made, and have sold the
# Software and the Larger Work(s), and to sublicense the foregoing rights on
# either these or other terms.
#
# This license is subject to the following condition:
#
# The above copyright notice and either this complete permission notice or at a
# minimum a reference to the UPL must be included in all copies or substantial
# portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
import sys
from . import CPyExtType, CPyExtHeapType

SlotsGetter = CPyExtType("SlotsGetter",
                         """
                         static PyObject* get_tp_attr(PyObject* unused, PyObject* object) {
                             return PyLong_FromVoidPtr(Py_TYPE(object)->tp_getattr);
                         }
                         static PyObject* get_tp_attro(PyObject* unused, PyObject* object) {
                             return PyLong_FromVoidPtr(Py_TYPE(object)->tp_getattro);
                         }
                         static PyObject* get_tp_descr_get(PyObject* unused, PyObject* object) {
                             return PyLong_FromVoidPtr(Py_TYPE(object)->tp_descr_get);
                         }
                         """,
                         tp_methods=
                         '{"get_tp_attr", (PyCFunction)get_tp_attr, METH_O | METH_STATIC, ""},' +
                         '{"get_tp_attro", (PyCFunction)get_tp_attro, METH_O | METH_STATIC, ""},' +
                         '{"get_tp_descr_get", (PyCFunction)get_tp_descr_get, METH_O | METH_STATIC, ""}')




def test_descr():
    TestDescrSetAndDel = CPyExtType("TestDescrSetAndDel",
                              '''
                              int testdescr_set(PyObject* self, PyObject* key, PyObject* value) {
                                  Py_XDECREF(((TestDescrSetAndDelObject*)self)->payload);
                                  if (value != NULL) {
                                      Py_INCREF(value);
                                  }
                                  ((TestDescrSetAndDelObject*)self)->payload = value;
                                  return 0;
                              }

                              PyObject* testdescr_get(PyObject* self, PyObject* key, PyObject* type) {
                                  PyObject* r = ((TestDescrSetAndDelObject*)self)->payload;
                                  if (r == NULL) {
                                      Py_RETURN_NONE;
                                  }
                                  Py_INCREF(r);
                                  return r;
                              }
                              ''',
                              cmembers='PyObject* payload;',
                              tp_descr_set="(descrsetfunc) testdescr_set",
                              tp_descr_get="(descrgetfunc) testdescr_get")
    class MyC:
        prop = TestDescrSetAndDel()

    x = MyC()
    x.prop = 42
    assert x.prop == 42
    del x.prop
    assert x.prop is None

    x = MyC()
    x.prop = 42
    assert x.prop == 42
    x.__delattr__('prop')
    assert x.prop is None

    raw = TestDescrSetAndDel()
    raw.__set__('foo', 42)
    assert raw.__get__(raw, 'foo') == 42
    assert raw.__get__(raw) == 42
    raw.__delete__(raw)
    assert raw.__get__(raw, 'foo') is None


def test_attrs():
    Dummy = CPyExtHeapType("DummyThatShouldInheritBuiltinGetattro",
                                   slots= ['{Py_nb_add, &myadd}'],
                                   code='PyObject* myadd(PyObject* a, PyObject *b) { Py_INCREF(b); return b; }')
    # Check that we inherit the slot value and do not wrap it with some indirection
    # This should ensure that fast-paths for builtin tp_getattro work correctly
    assert SlotsGetter.get_tp_attro(Dummy()) == SlotsGetter.get_tp_attro(object())
    assert Dummy.__getattribute__ == object.__getattribute__

    class AttrManaged:
        def __init__(self):
            self.bar = 1

        def __getattr__(self, item):
            return 42

    assert SlotsGetter.get_tp_attr(AttrManaged()) == 0
    assert SlotsGetter.get_tp_attro(AttrManaged()) != 0
    assert AttrManaged().bar == 1
    assert AttrManaged().foo == 42

    import sys
    TestAttrOStolenFromModule = CPyExtType("TestAttrOStolenFromModule",
                                           ready_code = 'TestAttrOStolenFromModuleType.tp_getattro = PyModule_Type.tp_getattro;')
    assert SlotsGetter.get_tp_attro(sys) == SlotsGetter.get_tp_attro(TestAttrOStolenFromModule())
    assert sys.__getattribute__.__objclass__ == type(sys)
    assert TestAttrOStolenFromModule.__getattribute__.__objclass__ == TestAttrOStolenFromModule

    TestAttrEmptyDummy = CPyExtType("TestAttrEmptyDummy")
    assert TestAttrEmptyDummy.__getattribute__ == object.__getattribute__

    TestAttrSet = CPyExtType("TestAttrSet",
                              '''
                              int testattro_set(PyObject* self, PyObject* key, PyObject* value) {
                                  Py_XDECREF(((TestAttrSetObject*)self)->payload);
                                  if (value != NULL) {
                                      Py_INCREF(value);
                                  }
                                  ((TestAttrSetObject*)self)->payload = value;
                                  return 0;
                              }

                              PyObject* testattro_get(PyObject* self, PyObject* key) {
                                  PyObject* r = ((TestAttrSetObject*)self)->payload;
                                  if (r == NULL) Py_RETURN_NONE;
                                  Py_INCREF(r);
                                  return r;
                              }
                              ''',
                              cmembers='PyObject* payload;',
                             tp_getattro="(getattrofunc) testattro_get",
                             tp_setattro="(setattrofunc) testattro_set")

    x = TestAttrSet()
    x.foo = 42
    assert x.foo == 42
    del x.foo
    assert x.foo is None


def test_concat_vs_add():
    # Inheritance of the Py_sq_concat slot:
    SqAdd = CPyExtHeapType("SqAdd",
                   slots= ['{Py_sq_concat, &concat}'],
                   code= 'PyObject* concat(PyObject* a, PyObject *b) { Py_INCREF(a); return a; }')
    x = SqAdd()

    assert x + x is x
    # TODO: assert _operator.concat(x, x) is x when _operator.concat is implemented
    assert x.__add__(x) is x

    class SqAddManaged(SqAdd): pass
    x = SqAddManaged()
    assert x + x is x
    assert x.__add__(x) is x
    # TODO: assert _operator.concat(x, x) is x when _operator.concat is implemented

    SqAddAndNbAdd = CPyExtHeapType("SqAddAndNbAdd",
                           slots= [
                               '{Py_sq_concat, &concat}',
                               '{Py_nb_add, &myadd}',
                           ],
                           code=
                               'PyObject* concat(PyObject* a, PyObject *b) { Py_INCREF(a); return a; }' +
                               'PyObject* myadd(PyObject* a, PyObject *b) { Py_INCREF(b); return b; }')

    x = SqAddAndNbAdd()
    y = SqAddAndNbAdd()
    assert x + y is y
    # TODO: assert _operator.concat(x, x) is x when _operator.concat is implemented


def test_incompatible_slots_assignment():
    def assert_type_error(code):
        try:
            code()
            assert False, "should have raised TypeError: attribute name must be string"
        except TypeError:
            pass

    HackySlotsWithBuiltin = CPyExtType("HackySlots",
                                       ready_code='HackySlotsType.tp_descr_get = (void*)PyBaseObject_Type.tp_getattro;')

    class BarAttr(HackySlotsWithBuiltin):
        def __init__(self):
            self.bar = 42

    class MyClassWithDescr:
        descr = BarAttr()

    assert SlotsGetter.get_tp_descr_get(HackySlotsWithBuiltin()) == SlotsGetter.get_tp_attro(object())
    assert BarAttr().__get__('bar') == 42
    assert_type_error(lambda: MyClassWithDescr().descr)

    class ManagedAttrO:
        def __getattribute__(self, item):
            return item

    try:
        sys.modules["test_incompatible_slots_assignment_managed"] = ManagedAttrO
        HackySlotsWithManaged = CPyExtType("HackySlotsWithManaged",
                                           ready_code="""
                                 PyTypeObject* ManagedAttrO = (PyTypeObject*) PyDict_GetItemString(PyImport_GetModuleDict(), "test_incompatible_slots_assignment_managed");
                                 HackySlotsWithManagedType.tp_descr_get = (void*)ManagedAttrO->tp_getattro;
                                 """)

        class FooAttr(HackySlotsWithManaged):
            def __init__(self):
                self.foo = 42

        class MyClassWithDescr2:
            descr = FooAttr()

        assert FooAttr().__get__('foo') == 42, FooAttr().__get__('foo')
        assert_type_error(lambda: MyClassWithDescr2().descr)
        # This does not hold on GraalPy, because we need a different closure, because the lookup result has changed:
        # assert SlotsGetter.get_tp_descr_get(HackySlotsWithManaged()) == SlotsGetter.get_tp_attro(ManagedAttrO())
    finally:
        sys.modules.pop("test_incompatible_slots_assignment_managed", None)
