%module "FGDBJNIWrapper"

%{

#include <stdexcept>
#include <sstream>
#include <iostream>
#include <stdio.h>
#include "time.h"
#include "FileGDBAPI.h"

std::string wstring2string(std::wstring wstr) {
  std::string str(wstr.length(),' ');
  copy(wstr.begin(),wstr.end(),str.begin());
  return str;
}

  
fgdbError checkResult(fgdbError error) {
  if (error) {
     std::wstring errorString;
     if (FileGDBAPI::ErrorInfo::GetErrorDescription(error, errorString) == S_FALSE) {
       throw std::runtime_error("Unknown error");
     } else {
       std::stringstream message;
       message << error << "\t" << wstring2string(errorString);
       FileGDBAPI::ErrorInfo::ClearErrors();
       throw std::runtime_error(message.str());
     }
  }
  return error;
}

void handleException(JNIEnv *jenv, const std::exception e) {
  std::stringstream message;
  message << e.what() ;
  jclass clazz = jenv->FindClass("java/lang/RuntimeException");
  jenv->ThrowNew(clazz, message.str().c_str());
}
  
void handleException(JNIEnv *jenv, const std::runtime_error e) {
  std::stringstream message;
  message << e.what() ;
  jclass clazz = jenv->FindClass("java/lang/RuntimeException");
  jenv->ThrowNew(clazz, message.str().c_str());
}

%}


%pragma(java) jniclasscode=%{
  static {
    System.loadLibrary("FGDBJNIWrapper");
    FGDBJNIWrapper.setMaxOpenFiles(2048);
  }
%}
%define EXT_FILEGDB_API
%enddef

%include "std_vector.i"
%include "std_string.i"
%include "std_wstring.i"
%include "typemaps.i"
%include "enums.swg"
%include "OutParam.i"
%include "OutArrayParam.i"
%javaconst(1);

%apply unsigned char *OUTPUT {byte *output};
%apply (char *STRING, size_t LENGTH)   { (char *byteArray, size_t length) };


%{
  struct byte_array {
    byte* bytes;
    size_t length;
  };
%}

%typemap(out) byte_array {
  jbyteArray buffer = JCALL1(NewByteArray, jenv, $1.length);
  jbyte* bytes = (jbyte*)$1.bytes;
  JCALL4(SetByteArrayRegion, jenv, buffer, 0, $1.length, bytes);
  delete $1.bytes;
  $result = buffer;
}
%typemap(jtype) byte_array "byte[]"
%typemap(jstype) byte_array "byte[]"
%typemap(jni) byte_array "jbyteArray"
%typemap(javaout) byte_array {
  return $jnicall;
}


%ignore byte_array;

%template(VectorOfString) std::vector<std::string>;
%template(VectorOfWString) std::vector<std::wstring>;
%template(VectorOfFieldDef) std::vector<FileGDBAPI::FieldDef>;

%include "Array.i"

%define linux
%enddef

%exception {
  try {
    $action;
  } catch (const std::runtime_error& e) {
    handleException(jenv, e);
  } catch (const std::exception& e) {
    handleException(jenv, e);
  }
}

%ignore FileGDBAPI::CreateGeodatabase;
%ignore FileGDBAPI::OpenGeodatabase;
%ignore FileGDBAPI::ErrorInfo::GetErrorRecordCount;
%ignore FileGDBAPI::ErrorInfo::GetErrorDescription;
%ignore FileGDBAPI::ErrorInfo::GetErrorRecord;
%ignore FileGDBAPI::ErrorInfo::ClearErrors;
%newobject createGeodatabase;
%newobject openGeodatabase;
%inline {
 
  void setMaxOpenFiles(int maxOpenFiles) {
#ifdef _WIN32
    _setmaxstdio(maxOpenFiles);
#endif
  }
  
  FileGDBAPI::Geodatabase* createGeodatabase(const std::wstring& path) {
    FileGDBAPI::Geodatabase* value = new FileGDBAPI::Geodatabase();
    checkResult(FileGDBAPI::CreateGeodatabase(path, *value));
    return value;
  }
  FileGDBAPI::Geodatabase* openGeodatabase(const std::wstring& path) {
    FileGDBAPI::Geodatabase* value = new FileGDBAPI::Geodatabase();
    checkResult(FileGDBAPI::OpenGeodatabase(path, *value));
    return value;
  }
  
  std::wstring getSpatialReferenceWkt(int srid) {
    FileGDBAPI::SpatialReferenceInfo value;
    FileGDBAPI::SpatialReferences::FindSpatialReferenceBySRID(srid, value);
    return value.srtext;
  }
}

%ignore FileGDBAPI::ShapeModifiers;
%ignore FileGDBAPI::CurveType;

%include "FileGDBCore.h"
%include "GeodatabaseManagement.h"

%rename(createGeodatabase2) FileGDBAPI::CreateGeodatabase;
%rename(openGeodatabase2) FileGDBAPI::OpenGeodatabase;
%rename(closeGeodatabase2) FileGDBAPI::CloseGeodatabase;
%rename(deleteGeodatabase2) FileGDBAPI::DeleteGeodatabase;

%typemap(javafinalize) FileGDBAPI::Geodatabase %{
   protected void finalize() {
   }
%}
%typemap(javafinalize) FileGDBAPI::Table %{
   protected void finalize() {
   }
%}
%ignore FileGDBAPI::Geodatabase::ExecuteSQL;
%ignore FileGDBAPI::Geodatabase::GetDatasetDefinition;
%ignore FileGDBAPI::Geodatabase::GetDatasetDocumentation;
%ignore FileGDBAPI::Geodatabase::GetQueryName;
%ignore FileGDBAPI::Geodatabase::OpenTable;
%ignore FileGDBAPI::Geodatabase::CloseTable;
%ignore FileGDBAPI::Geodatabase::CreateTable;
%ignore FileGDBAPI::Geodatabase::CreateDomain;
%ignore FileGDBAPI::Geodatabase::AlterDomain;
%ignore FileGDBAPI::Geodatabase::DeleteDomain;
%ignore FileGDBAPI::Geodatabase::GetDomainDefinition;
%ignore FileGDBAPI::Geodatabase::GetDomains;
%ignore FileGDBAPI::Geodatabase::GetChildDatasets;
%ignore FileGDBAPI::Geodatabase::CreateFeatureDataset;
%newobject FileGDBAPI::Geodatabase::createTable;
%newobject FileGDBAPI::Geodatabase::openTable;
%extend FileGDBAPI::Geodatabase {
  void createFeatureDataset(std::string featureDatasetDef) {
    checkResult(self->CreateFeatureDataset(featureDatasetDef));
  }

  FileGDBAPI::EnumRows* query(const std::wstring& sql, bool recycling) {
    FileGDBAPI::EnumRows* rows = new FileGDBAPI::EnumRows();
    checkResult(self->ExecuteSQL(sql, recycling, *rows));
    return rows;
  }

  std::vector<std::wstring> getChildDatasets(std::wstring parentPath, std::wstring datasetType) {
    std::vector<std::wstring> value;
    checkResult(self->GetChildDatasets(parentPath, datasetType, value));
    return value;
  }
  
  std::string getDatasetDefinition(std::wstring path, std::wstring datasetType) {
    std::string value;
    checkResult(self->GetDatasetDefinition(path, datasetType, value));
    return value;
  }
  
  std::string getDatasetDocumentation(std::wstring path, std::wstring datasetType) {
    std::string value;
    checkResult(self->GetDatasetDocumentation(path, datasetType, value));
    return value;
  }
  
  std::vector<std::wstring> getDomains() {
    std::vector<std::wstring> value;
    checkResult(self->GetDomains(value));
    return value;
  }
  
  std::string getDomainDefinition(std::wstring domainName) {
    std::string value;
    checkResult(self->GetDomainDefinition(domainName, value));
    return value;
  }
  void createDomain(const std::string& domainDefinition) {
    checkResult(self->CreateDomain(domainDefinition));
  }
  void alterDomain(const std::string& domainDefinition) {
    checkResult(self->AlterDomain(domainDefinition));
  }
  void deleteDomain(const std::wstring& domainName) {
    checkResult(self->DeleteDomain(domainName));
  }
  
  std::wstring getQueryName(std::wstring path) {
    std::wstring value;
    checkResult(self->GetQueryName(path, value));
    return value;
  }
  FileGDBAPI::Table* openTable(const std::wstring& path) {
    FileGDBAPI::Table* value = new FileGDBAPI::Table();
    checkResult(self->OpenTable(path, *value));
    return value;
  }
  void closeTable(FileGDBAPI::Table& table) {
    checkResult(self->CloseTable(table));
  }
  std::string getTableDefinition(const std::wstring& path) {
    FileGDBAPI::Table table;
    checkResult(self->OpenTable(path, table));
    std::string tableDefinition;
    checkResult(table.GetDefinition(tableDefinition));
    checkResult(self->OpenTable(path, table));
    
    return tableDefinition;
  }
  FileGDBAPI::Table* createTable(const std::string& tableDefinition, const std::wstring& parent) {
    FileGDBAPI::Table* value = new FileGDBAPI::Table();
    checkResult(self->CreateTable(tableDefinition, parent, *value));
    return value;
  }
}
%include "Geodatabase.h"

%ignore FileGDBAPI::Table::GetFields;
%ignore FileGDBAPI::Table::IsEditable;
%ignore FileGDBAPI::Table::GetDefinition;
%ignore FileGDBAPI::Table::GetDocumentation;
%ignore FileGDBAPI::Table::GetRowCount;
%ignore FileGDBAPI::Table::GetDefaultSubtypeCode;
%ignore FileGDBAPI::Table::CreateRowObject;
%ignore FileGDBAPI::Table::GetIndexes;
%ignore FileGDBAPI::Table::Search;
%ignore FileGDBAPI::Table::Insert;
%ignore FileGDBAPI::Table::Update;
%ignore FileGDBAPI::Table::Delete;
%ignore FileGDBAPI::Table::SetWriteLock;
%ignore FileGDBAPI::Table::FreeWriteLock;
%ignore FileGDBAPI::Table::LoadOnlyMode;
%ignore FileGDBAPI::Table::Delete;
%newobject FileGDBAPI::Table::createRowObject;
%newobject FileGDBAPI::Table::search;
%extend FileGDBAPI::Table {
  bool isEditable() {
    bool value;
    checkResult(self->IsEditable(value));
    return value;
  }
  std::string getDefinition() {
    std::string value;
    checkResult(self->GetDefinition(value));
    return value;
  }
  std::string getDocumentation() {
    std::string value;
    checkResult(self->GetDocumentation(value));
    return value;
  }
  int getRowCount() {
    int value;
    checkResult(self->GetRowCount(value));
    return value;
  }
  int getDefaultSubtypeCode() {
    int value;
    checkResult(self->GetDefaultSubtypeCode(value));
    return value;
  }
  std::vector<std::string> getIndexes() {
    std::vector<std::string> value;
    checkResult(self->GetIndexes(value));
    return value;
  }
  FileGDBAPI::Row* createRowObject() {
    FileGDBAPI::Row* value = new FileGDBAPI::Row();
    checkResult(self->CreateRowObject(*value));
    return value;
  }
  void insertRow(FileGDBAPI::Row& row) {
    checkResult(self->Insert(row));
  }
  void updateRow(FileGDBAPI::Row& row) {
    checkResult(self->Update(row));
  }
  void deleteRow(FileGDBAPI::Row& row) {
    checkResult(self->Delete(row));
  }

  FileGDBAPI::EnumRows* search(const std::wstring& subfields, const std::wstring& whereClause, Envelope envelope, bool recycling) {
    FileGDBAPI::EnumRows* rows = new FileGDBAPI::EnumRows();
    checkResult(self->Search(subfields, whereClause, envelope, recycling, *rows));
    return rows;
  }

  FileGDBAPI::EnumRows* search(const std::wstring& subfields, const std::wstring& whereClause, bool recycling) {
    FileGDBAPI::EnumRows* rows = new FileGDBAPI::EnumRows();
    checkResult(self->Search(subfields, whereClause, recycling, *rows));
    return rows;
  }
  void setLoadOnlyMode(bool loadOnly) {
    checkResult(self->LoadOnlyMode(loadOnly));
  }
  void setWriteLock() {
    checkResult(self->SetWriteLock());
  }
  void freeWriteLock() {
    checkResult(self->FreeWriteLock());
  }

  std::vector<FileGDBAPI::FieldDef> getFields() {
    std::vector<FileGDBAPI::FieldDef> value;
    checkResult(self->GetFields(value));
    return value;
  }
}
%include "Table.h"

%ignore FileGDBAPI::Row::IsNull;
%ignore FileGDBAPI::Row::SetNull;
%ignore FileGDBAPI::Row::GetDate;
%ignore FileGDBAPI::Row::SetDate;
%ignore FileGDBAPI::Row::GetDouble;
%ignore FileGDBAPI::Row::SetDouble;
%ignore FileGDBAPI::Row::GetFloat;
%ignore FileGDBAPI::Row::SetFloat;
%ignore FileGDBAPI::Row::GetBinary;
%ignore FileGDBAPI::Row::SetBinary;
%ignore FileGDBAPI::Row::GetGeometry;
%ignore FileGDBAPI::Row::SetGeometry;
%ignore FileGDBAPI::Row::GetGUID;
%ignore FileGDBAPI::Row::SetGUID;
%ignore FileGDBAPI::Row::GetGlobalID;
%ignore FileGDBAPI::Row::SetGlobalID;
%ignore FileGDBAPI::Row::GetInteger;
%ignore FileGDBAPI::Row::SetInteger;
%ignore FileGDBAPI::Row::GetOID;
%ignore FileGDBAPI::Row::SetOID;
%ignore FileGDBAPI::Row::GetRaster;
%ignore FileGDBAPI::Row::SetRaster;
%ignore FileGDBAPI::Row::GetShort;
%ignore FileGDBAPI::Row::SetShort;
%ignore FileGDBAPI::Row::GetString;
%ignore FileGDBAPI::Row::SetString;
%ignore FileGDBAPI::Row::GetXML;
%ignore FileGDBAPI::Row::SetXML;
%ignore FileGDBAPI::Row::GetFields;
%extend FileGDBAPI::Row {
  bool isNull(std::wstring name) {
    bool value;
    checkResult(self->IsNull(name,value));
    return value;
  }
  void setNull(std::wstring name) {
    checkResult(self->SetNull(name));
  }
 
  long long getDate(const std::wstring& name) {
    struct tm value;
    checkResult(self->GetDate(name,value));
    return mktime(&value);
  }
  void setDate(const std::wstring& name, long long date) {
    const time_t time = (time_t)date;
    struct tm* tm_time = localtime(&time);
    if (tm_time == 0) {
      std::stringstream message;
      message << "Invalid date ";
      message << date;
      throw std::runtime_error(message.str());
    } else {
      struct tm value;
      value = *tm_time;
      checkResult(self->SetDate(name, value));
    }
  }

  double getDouble(const std::wstring& name) {
    double value;
    checkResult(self->GetDouble(name,value));
    return value;
  }
  
  void setDouble(const std::wstring& name, double value) {
    checkResult(self->SetDouble(name, value));
  }
 
  float getFloat(const std::wstring& name) {
    float value;
    checkResult(self->GetFloat(name,value));
    return value;
  }
  
  void setFloat(const std::wstring& name, double value) {
    checkResult(self->SetFloat(name, value));
  }

  FileGDBAPI::Guid getGuid(std::wstring name) {
    FileGDBAPI::Guid value;
    checkResult(self->GetGUID(name,value));
    return value;
  }
 
  FileGDBAPI::Guid getGlobalId() {
    FileGDBAPI::Guid value;
    checkResult(self->GetGlobalID(value));
    return value;
  }
  
  void setGuid(const std::wstring& name, const FileGDBAPI::Guid& value) {
    checkResult(self->SetGUID(name, value));
  }

  int getOid() {
    int value;
    checkResult(self->GetOID(value));
    return value;
  }

  short getShort(const std::wstring& name) {
    short value;
    checkResult(self->GetShort(name,value));
    return value;
  }
  
  void setShort(const std::wstring& name, short value) {
    checkResult(self->SetShort(name, value));
  }

  int32 getInteger(const std::wstring& name) {
    int value;
    checkResult(self->GetInteger(name,value));
    return value;
  }
    
  void setInteger(const std::wstring& name, int32 value) {
    checkResult(self->SetInteger(name, value));
  }
  
  std::wstring getString(const std::wstring& name) {
    std::wstring value;
    checkResult(self->GetString(name,value));
    return value;
  }
  
  void setString(const std::wstring& name, const std::wstring& value) {
    checkResult(self->SetString(name, value));
  }

  std::string getXML(const std::wstring& name) {
    std::string value;
    checkResult(self->GetXML(name,value));
    return value;
  }
  
  void setXML(const std::wstring& name, const std::string& value) {
    checkResult(self->SetXML(name, value));
  }

  byte_array getGeometry() {
    FileGDBAPI::ShapeBuffer geometry;
    checkResult(self->GetGeometry(geometry));
    
    byte_array buffer;
    buffer.bytes = new byte[geometry.inUseLength];
    memcpy(buffer.bytes, geometry.shapeBuffer, geometry.inUseLength);
    buffer.length = geometry.inUseLength;
    
    return buffer;
  }
   
  void setGeometry(char* byteArray, size_t length) {
    FileGDBAPI::ShapeBuffer shape;
    shape.Allocate(length);
    for (int i = 0; i < length; i++) {
      char c = byteArray[i];
      shape.shapeBuffer[i] = (byte)c;
    }
    shape.inUseLength = length;
    checkResult(self->SetGeometry(shape));
  }
   
  std::vector<FileGDBAPI::FieldDef> getFields() {
    std::vector<FileGDBAPI::FieldDef> value;
    checkResult(self->GetFields(value));
    return value;
  }
  
}

%include "Row.h"

%rename(equal) FileGDBAPI::Guid::operator==;
%ignore FileGDBAPI::Guid::data1;
%ignore FileGDBAPI::Guid::data2;
%ignore FileGDBAPI::Guid::data3;
%ignore FileGDBAPI::Guid::data4;
%rename(notEqual) FileGDBAPI::Guid::operator!=;

%ignore FileGDBAPI::FieldInfo::GetFieldCount;
%ignore FileGDBAPI::FieldInfo::GetFieldName;
%ignore FileGDBAPI::FieldInfo::GetFieldLength;
%ignore FileGDBAPI::FieldInfo::GetFieldType;
%ignore FileGDBAPI::FieldInfo::GetFieldIsNullable;
%extend FileGDBAPI::FieldInfo {
  int getFieldCount() {
    int count;
    checkResult(self->GetFieldCount(count));
    return count;
  }

  std::wstring getFieldName(int i) {
    std::wstring name;
    checkResult(self->GetFieldName(i, name));
    return name;
  }

  int getFieldLength(int i) {
    int length;
    checkResult(self->GetFieldLength(i, length));
    return length;
  }
  bool isNullable(int i) {
    bool nullable;
    checkResult(self->GetFieldIsNullable(i, nullable));
    return nullable;
  }

  FileGDBAPI::FieldType getFieldType(int i) {
    FileGDBAPI::FieldType type;
    checkResult(self->GetFieldType(i, type));
    return type;
  }

}


%ignore FileGDBAPI::FieldDef::GetAlias;
%ignore FileGDBAPI::FieldDef::GetName;
%ignore FileGDBAPI::FieldDef::GetLength;
%ignore FileGDBAPI::FieldDef::GetIsNullable;
%ignore FileGDBAPI::FieldDef::GetType;
%extend FileGDBAPI::FieldDef {
  std::wstring getAlias() {
    std::wstring value;
    checkResult(self->GetAlias(value));
    return value;
  }
  std::wstring getName() {
    std::wstring value;
    checkResult(self->GetName(value));
    return value;
  }
  bool isNullable() {
    bool result;
    checkResult(self->GetIsNullable(result));
    return result;
  }
  int getLength() {
    int result;
    checkResult(self->GetLength(result));
    return result;
  }
  FileGDBAPI::FieldType getType() {
    FileGDBAPI::FieldType result;
    checkResult(self->GetType(result));
    return result;
  }
}

%ignore FileGDBAPI::GeometryDef::GetGeometryType;
%ignore FileGDBAPI::GeometryDef::GetHasM;
%ignore FileGDBAPI::GeometryDef::GetHasZ;
%extend FileGDBAPI::GeometryDef {
  bool hasM() {
    bool result;
    checkResult(self->GetHasM(result));
    return result;
  }
  bool hasZ() {
    bool result;
    checkResult(self->GetHasZ(result));
    return result;
  }
  FileGDBAPI::GeometryType getGeometryType() {
    FileGDBAPI::GeometryType result;
    checkResult(self->GetGeometryType(result));
    return result;
  }
}
%ignore FileGDBAPI::SpatialReference::GetSpatialReferenceID;
%ignore FileGDBAPI::SpatialReference::GetSpatialReferenceText;
%ignore FileGDBAPI::SpatialReference::GetXYFalseOriginAndUnits;
//%ignore FileGDBAPI::SpatialReference::GetMFalseOriginAndUnits;
%ignore FileGDBAPI::SpatialReference::GetMTolerance;
%ignore FileGDBAPI::SpatialReference::GetXYTolerance;
//%ignore FileGDBAPI::SpatialReference::GetZFalseOriginAndUnits;
%ignore FileGDBAPI::SpatialReference::GetZTolerance;

%ignore FileGDBAPI::SpatialReference::GetXYFalseOrigin;
%ignore FileGDBAPI::SpatialReference::GetZFalseOrigin;
%ignore FileGDBAPI::SpatialReference::GetMFalseOrigin;

%ignore FileGDBAPI::SpatialReference::GetMResolution;
%ignore FileGDBAPI::SpatialReference::GetXResolution;
%ignore FileGDBAPI::SpatialReference::GetXYResolution;
%ignore FileGDBAPI::SpatialReference::GetYResolution;
%ignore FileGDBAPI::SpatialReference::GetZResolution;

%extend FileGDBAPI::SpatialReference {
  int getId() {
    int result;
    checkResult(self->GetSpatialReferenceID(result));
    return result;
  }
  std::wstring getText() {
    std::wstring value;
    checkResult(self->GetSpatialReferenceText(value));
    return value;
  }
  double getXFalseOrigin() {
    double falseX;
    double falseY;
    checkResult(self->GetXYFalseOrigin(falseX, falseY));
    return falseX;
  }
  double getYFalseOrigin() {
    double falseX;
    double falseY;
    checkResult(self->GetXYFalseOrigin(falseX, falseY));
    return falseY;
  }
  double getMFalseOrigin() {
    double falseM;
    double mUnits;
    checkResult(self->GetMFalseOrigin(falseM));
    return falseM;
  }
  double getMTolerance() {
    double result;
    checkResult(self->GetMTolerance(result));
    return result;
  }
  double getXYTolerance() {
    double result;
    checkResult(self->GetXYTolerance(result));
    return result;
  }
  double getZTolerance() {
    double result;
    checkResult(self->GetZTolerance(result));
    return result;
  }
  double getMResolution() {
    double result;
    checkResult(self->GetMResolution(result));
    return result;
  }
  double getZResolution() {
    double result;
    checkResult(self->GetZResolution(result));
    return result;
  }
  double getXYResolution() {
    double result;
    checkResult(self->GetXYResolution(result));
    return result;
  }

}

%ignore FileGDBAPI::IndexDef::GetIsUnique;
%ignore FileGDBAPI::IndexDef::GetName;
%ignore FileGDBAPI::IndexDef::GetFields;
%extend FileGDBAPI::IndexDef {
  bool isUnique() {
    bool result;
    checkResult(self->GetIsUnique(result));
    return result;
  }
  std::wstring getName() {
    std::wstring result;
    checkResult(self->GetName(result));
    return result;
  }
  std::wstring getFields() {
    std::wstring result;
    checkResult(self->GetFields(result));
    return result;
  }
}

%ignore FileGDBAPI::Guid::ToString;
%extend FileGDBAPI::Guid {
  std::wstring toString() {
    std::wstring value;
    checkResult(self->ToString(value));
    return value;
  }
}

%ignore FileGDBAPI::ByteArray;
%ignore FileGDBAPI::ShapeBuffer;

%ignore FileGDBAPI::EnumSpatialReferenceInfo;
%ignore FileGDBAPI::SpatialReferenceInfo;
%ignore FileGDBAPI::SpatialReferences::FindSpatialReferenceByName;
%ignore FileGDBAPI::SpatialReferences::FindSpatialReferenceBySRID;

%ignore FileGDBAPI::Point;
%ignore FileGDBAPI::PointShapeBuffer;
%ignore FileGDBAPI::MultiPointShapeBuffer;
%ignore FileGDBAPI::MultiPartShapeBuffer;
%ignore FileGDBAPI::MultiPatchShapeBuffer;

%ignore FileGDBAPI::EnumRows::Next;
%ignore FileGDBAPI::EnumRows::GetFields;
%newobject FileGDBAPI::EnumRows::next;

%typemap(javafinalize) FileGDBAPI::EnumRows %{
   protected void finalize() {
   }
%}
%extend FileGDBAPI::EnumRows {
  FileGDBAPI::Row* next() {
    FileGDBAPI::Row* value = new FileGDBAPI::Row();
    int hr = self->Next(*value);
    if (hr == S_OK) {
      return value;
    } else {
      delete value;
      return NULL;
    }
  }

  std::vector<FileGDBAPI::FieldDef> getFields() {
    std::vector<FileGDBAPI::FieldDef> value;
    checkResult(self->GetFields(value));
    return value;
  }
}

%ignore FileGDBAPI::Curve;
%ignore FileGDBAPI::CircularArcCurve;
%ignore FileGDBAPI::BezierCurve;
%ignore FileGDBAPI::EllipticArcCurve;

%include "Util.h"

%include "Raster.h"


