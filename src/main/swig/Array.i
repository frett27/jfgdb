%inline %{
template<class T> class ArrayOut {
  public:
    T* arrayOut;

    ArrayOut() {
    };
    
    const T& get(int i) {
      return this->arrayOut[i];
    };
    
    void set(int i, const T& value) {
      this->arrayOut[i] = value;
    };
};
%}
%ignore *::arrayOut;

%define ARRAY_OUT(C_TYPE, WRAPPER_TYPE)

%template(WRAPPER_TYPE) ArrayOut<C_TYPE>;

%typemap(out) C_TYPE* {

    ArrayOut<C_TYPE>* arrayOut = new ArrayOut<C_TYPE>();
    arrayOut->arrayOut = $1;
    *(ArrayOut<C_TYPE> **)&$result = arrayOut;
}
%typemap(jstype) C_TYPE* "WRAPPER_TYPE"
%typemap(jni) C_TYPE* "jlong"
%typemap(javaout) C_TYPE* {
  return new WRAPPER_TYPE($jnicall, $owner);
}
%enddef
