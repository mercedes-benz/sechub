# ProjectApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**userApprovesJob**](ProjectApi.md#userApprovesJob) | **PUT** /api/project/{projectId}/job/{jobUUID}/approve | User approves sechub job
[**userChecksJobStatus**](ProjectApi.md#userChecksJobStatus) | **GET** /api/project/{projectId}/job/{jobUUID} | User checks sechub job state
[**userCreatesNewJob**](ProjectApi.md#userCreatesNewJob) | **POST** /api/project/{projectId}/job | User creates a new sechub job
[**userDefinesProjectMockdata**](ProjectApi.md#userDefinesProjectMockdata) | **PUT** /api/project/{projectId}/mockdata | User defines mock data configuration for project
[**userDownloadsJobReport**](ProjectApi.md#userDownloadsJobReport) | **GET** /api/project/{projectId}/report/{jobUUID} | User downloads sechub job report
[**userFetchesFalsePositiveConfigurationOfProject**](ProjectApi.md#userFetchesFalsePositiveConfigurationOfProject) | **GET** /api/project/{projectId}/false-positives | User fetches false positive configuration of project
[**userMarksFalsePositivesForJob**](ProjectApi.md#userMarksFalsePositivesForJob) | **PUT** /api/project/{projectId}/false-positives | User marks false positives for finished sechub job
[**userRetrievesProjectMockdata**](ProjectApi.md#userRetrievesProjectMockdata) | **GET** /api/project/{projectId}/mockdata | User retrieves mock data configuration for project
[**userUnmarksFalsePositives**](ProjectApi.md#userUnmarksFalsePositives) | **DELETE** /api/project/{projectId}/false-positive/{jobUUID}/{findingId} | User unmarks existing false positive definitons
[**userUploadsSourceCode**](ProjectApi.md#userUploadsSourceCode) | **POST** /api/project/{projectId}/job/{jobUUID}/sourcecode | User uploads source code


<a name="userApprovesJob"></a>
# **userApprovesJob**
> userApprovesJob(projectId, jobUUID)

User approves sechub job

User approves sechub job

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The id of the project where sechub job shall be approved
    String jobUUID = "jobUUID_example"; // String | The jobUUID for sechub job
    try {
      apiInstance.userApprovesJob(projectId, jobUUID);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userApprovesJob");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The id of the project where sechub job shall be approved |
 **jobUUID** | **String**| The jobUUID for sechub job |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userChecksJobStatus"></a>
# **userChecksJobStatus**
> JobStatus userChecksJobStatus(projectId, jobUUID)

User checks sechub job state

User checks sechub job state

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The id of the project where sechub job was started for
    String jobUUID = "jobUUID_example"; // String | The jobUUID for sechub job
    try {
      JobStatus result = apiInstance.userChecksJobStatus(projectId, jobUUID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userChecksJobStatus");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The id of the project where sechub job was started for |
 **jobUUID** | **String**| The jobUUID for sechub job |

### Return type

[**JobStatus**](JobStatus.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userCreatesNewJob"></a>
# **userCreatesNewJob**
> JobId userCreatesNewJob(projectId, scanJob)

User creates a new sechub job

User creates a new sechub job

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The unique id of the project id where a new sechub job shall be created
    ScanJob scanJob = {"webScan":{"login":{"url":"https://localhost/mywebapp/login","basic":{"user":"username1","password":"password1"}},"uris":["https://localhost/mywebapp"]},"apiVersion":"1.0"}; // ScanJob | 
    try {
      JobId result = apiInstance.userCreatesNewJob(projectId, scanJob);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userCreatesNewJob");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The unique id of the project id where a new sechub job shall be created |
 **scanJob** | [**ScanJob**](ScanJob.md)|  | [optional]

### Return type

[**JobId**](JobId.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json;charset=UTF-8
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userDefinesProjectMockdata"></a>
# **userDefinesProjectMockdata**
> userDefinesProjectMockdata(projectId, body)

User defines mock data configuration for project

User defines mock data configuration for project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | 
    Object body = {"codeScan":{"result":"RED"},"webScan":{"result":"YELLOW"},"infraScan":{"result":"GREEN"}}; // Object | 
    try {
      apiInstance.userDefinesProjectMockdata(projectId, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userDefinesProjectMockdata");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**|  |
 **body** | **Object**|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json;charset=UTF-8
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userDownloadsJobReport"></a>
# **userDownloadsJobReport**
> Object userDownloadsJobReport(projectId, jobUUID)

User downloads sechub job report

User downloads sechub job report

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The project Id
    String jobUUID = "jobUUID_example"; // String | The job UUID
    try {
      Object result = apiInstance.userDownloadsJobReport(projectId, jobUUID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userDownloadsJobReport");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The project Id |
 **jobUUID** | **String**| The job UUID |

### Return type

**Object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, text/html;charset=UTF-8

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userFetchesFalsePositiveConfigurationOfProject"></a>
# **userFetchesFalsePositiveConfigurationOfProject**
> FalsePositives userFetchesFalsePositiveConfigurationOfProject(projectId)

User fetches false positive configuration of project

User fetches false positive configuration of project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The project id
    try {
      FalsePositives result = apiInstance.userFetchesFalsePositiveConfigurationOfProject(projectId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userFetchesFalsePositiveConfigurationOfProject");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The project id |

### Return type

[**FalsePositives**](FalsePositives.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userMarksFalsePositivesForJob"></a>
# **userMarksFalsePositivesForJob**
> userMarksFalsePositivesForJob(projectId, falsePositivesForJob)

User marks false positives for finished sechub job

User marks false positives for finished sechub job

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The projectId of the project where users adds false positives for
    FalsePositivesForJob falsePositivesForJob = {"apiVersion":"1.0","type":"falsePositiveJobDataList","jobData":[{"jobUUID":"f1d02a9d-5e1b-4f52-99e5-401854ccf936","findingId":42,"comment":"an optional comment why this is a false positive..."}]}; // FalsePositivesForJob | 
    try {
      apiInstance.userMarksFalsePositivesForJob(projectId, falsePositivesForJob);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userMarksFalsePositivesForJob");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The projectId of the project where users adds false positives for |
 **falsePositivesForJob** | [**FalsePositivesForJob**](FalsePositivesForJob.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json;charset=UTF-8
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userRetrievesProjectMockdata"></a>
# **userRetrievesProjectMockdata**
> Object userRetrievesProjectMockdata(projectId)

User retrieves mock data configuration for project

User retrieves mock data configuration for project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | 
    try {
      Object result = apiInstance.userRetrievesProjectMockdata(projectId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userRetrievesProjectMockdata");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**|  |

### Return type

**Object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userUnmarksFalsePositives"></a>
# **userUnmarksFalsePositives**
> userUnmarksFalsePositives(projectId, jobUUID, findingId)

User unmarks existing false positive definitons

User unmarks existing false positive definitons

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The project id
    String jobUUID = "jobUUID_example"; // String | Job uuid
    String findingId = "findingId_example"; // String | Finding id - in combination with job UUID this defines the false positive to remove
    try {
      apiInstance.userUnmarksFalsePositives(projectId, jobUUID, findingId);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userUnmarksFalsePositives");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The project id |
 **jobUUID** | **String**| Job uuid |
 **findingId** | **String**| Finding id - in combination with job UUID this defines the false positive to remove |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="userUploadsSourceCode"></a>
# **userUploadsSourceCode**
> userUploadsSourceCode(projectId, jobUUID, checkSum)

User uploads source code

User uploads source code

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    String projectId = "projectId_example"; // String | The id of the project where sourcecode shall be uploaded for
    String jobUUID = "jobUUID_example"; // String | The jobUUID for sechub job
    String checkSum = "checkSum_example"; // String | A sha256 checksum for file upload validation
    try {
      apiInstance.userUploadsSourceCode(projectId, jobUUID, checkSum);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#userUploadsSourceCode");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectId** | **String**| The id of the project where sourcecode shall be uploaded for |
 **jobUUID** | **String**| The jobUUID for sechub job |
 **checkSum** | **String**| A sha256 checksum for file upload validation |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

