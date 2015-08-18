%inline %{
template<class T> class OutArrayParamValue {
  public:
    T* outArrayParamValue;

    OutArrayParamValue() {
    };
    
    const T& get(int i) {
      return this->outArrayParamValue[i];
    };
    
    void set(int i, const T& value) {
      this->outArrayParamValue[i] = value;
    };
};
%}
%ignore *::outArrayParamValue;

%define OUT_ARRAY_PARAM(C_TYPE, WRAPPER_TYPE)

%template(WRAPPER_TYPE) OutArrayParamValue<C_TYPE>;

%typemap(in) C_TYPE* &OUTVALUE {
    jclass clazz = jenv->GetObjectClass($input);
    jfieldID cPointerField = jenv->GetFieldID(clazz, "swigCPtr", "J");
    jlong cPtr = jenv->GetLongField($input, cPointerField);
    OutArrayParamValue<C_TYPE> *wrapper = NULL;
    *(OutArrayParamValue<C_TYPE> **)&wrapper = *(OutArrayParamValue<C_TYPE> **)&cPtr;
    $1 = &wrapper->outArrayParamValue;
}

%typemap(jtype) C_TYPE* &OUTVALUE "WRAPPER_TYPE"
%typemap(jstype) C_TYPE* &OUTVALUE "WRAPPER_TYPE"
%typemap(jni) C_TYPE* &OUTVALUE "jobject"

%typemap(javain) C_TYPE* &OUTVALUE "$javainput"

%apply C_TYPE* &OUTVALUE { C_TYPE*&};

%enddef
