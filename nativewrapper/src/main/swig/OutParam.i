%inline %{
template<class T> class OutParamValue {
  public:
    T value;
};
%}
%ignore *::getOutParamValueInstance;

%define OUT_PARAM(C_TYPE, WRAPPER_TYPE)

%template(WRAPPER_TYPE) OutParamValue<C_TYPE>;

%typemap(in) C_TYPE &OUTVALUE {
    jclass clazz = jenv->GetObjectClass($input);
    jfieldID cPointerField = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, cPointerField);
    OutParamValue<C_TYPE> *wrapper = NULL;
    *(OutParamValue<C_TYPE> **)&wrapper = *(OutParamValue<C_TYPE> **)&cPtr;
    $1 = &wrapper->value;
}

%typemap(jtype) C_TYPE &OUTVALUE "WRAPPER_TYPE"
%typemap(jstype) C_TYPE &OUTVALUE "WRAPPER_TYPE"
%typemap(jni) C_TYPE &OUTVALUE "jobject"

%typemap(javain) C_TYPE &OUTVALUE "$javainput"

%apply C_TYPE &OUTVALUE { C_TYPE&};

%enddef
