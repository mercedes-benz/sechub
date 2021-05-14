# AdminApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**adminAcceptsSignup**](AdminApi.md#adminAcceptsSignup) | **POST** /api/admin/signup/accept/{userId} | Admin applies self registration
[**adminAssignsExecutionProfileToProject**](AdminApi.md#adminAssignsExecutionProfileToProject) | **POST** /api/admin/config/execution/profile/{profileId}/project/{projectId} | Admin assigns execution profile to project
[**adminAssignsUserToProject**](AdminApi.md#adminAssignsUserToProject) | **POST** /api/admin/project/{userId}/membership/{projectId} | Admin assigns user to project
[**adminCancelsJob**](AdminApi.md#adminCancelsJob) | **POST** /api/admin/jobs/cancel/{jobUUID} | Admin cancels a job
[**adminChangesProjectOwner**](AdminApi.md#adminChangesProjectOwner) | **POST** /api/admin/project/{userId}/owner/{projectId} | Admin changes owner of a project
[**adminChecksServerVersion**](AdminApi.md#adminChecksServerVersion) | **GET** /api/admin/info/version | Admin checks server version
[**adminCreatesExecutionProfile**](AdminApi.md#adminCreatesExecutionProfile) | **POST** /api/admin/config/execution/profile/{profileId} | Admin creates an execution proflie
[**adminCreatesExecutorConfiguration**](AdminApi.md#adminCreatesExecutorConfiguration) | **POST** /api/admin/config/executor | Admin creates an executor configuration
[**adminCreatesProject**](AdminApi.md#adminCreatesProject) | **POST** /api/admin/project | Admin creates a project
[**adminDeleteProject**](AdminApi.md#adminDeleteProject) | **DELETE** /api/admin/project/{projectId} | Admin deletes a project
[**adminDeletesExecutionProfile**](AdminApi.md#adminDeletesExecutionProfile) | **DELETE** /api/admin/config/execution/profile/{profileId} | Admin deletes execution profile
[**adminDeletesExecutorConfiguration**](AdminApi.md#adminDeletesExecutorConfiguration) | **DELETE** /api/admin/config/executor/{uuid} | Admin deletes executor configuration
[**adminDeletesSignup**](AdminApi.md#adminDeletesSignup) | **DELETE** /api/admin/signup/{userId} | Admin deletes user signup
[**adminDeletesUser**](AdminApi.md#adminDeletesUser) | **DELETE** /api/admin/user/{userId} | Admin deletes a user
[**adminDisablesSchedulerJobProcessing**](AdminApi.md#adminDisablesSchedulerJobProcessing) | **POST** /api/admin/scheduler/disable/job-processing | Admin disables job processing in scheduler
[**adminDownloadsFullScanDataForJob**](AdminApi.md#adminDownloadsFullScanDataForJob) | **GET** /api/admin/scan/download/{jobUUID} | Admin downloads all details about a scan job
[**adminEnablesSchedulerJobProcessing**](AdminApi.md#adminEnablesSchedulerJobProcessing) | **POST** /api/admin/scheduler/enable/job-processing | Admin enables scheduler job processing
[**adminFetchesExecutionProfile**](AdminApi.md#adminFetchesExecutionProfile) | **GET** /api/admin/config/execution/profile/{profileId} | Admin fetches execution profile
[**adminFetchesExecutionProfileList**](AdminApi.md#adminFetchesExecutionProfileList) | **GET** /api/admin/config/execution/profiles | Admin fetches execution proflie list
[**adminFetchesExecutorConfiguration**](AdminApi.md#adminFetchesExecutorConfiguration) | **GET** /api/admin/config/executor/{uuid} | Admin fetches executor configuration
[**adminFetchesExecutorConfigurationList**](AdminApi.md#adminFetchesExecutorConfigurationList) | **GET** /api/admin/config/executors | Admin fetches executor configuration list
[**adminFetchesMappingConfiguration**](AdminApi.md#adminFetchesMappingConfiguration) | **GET** /api/admin/config/mapping/{mappingId} | Admin fetches mapping configuration
[**adminGrantsAdminRightsToUser**](AdminApi.md#adminGrantsAdminRightsToUser) | **POST** /api/admin/user/{userId}/grant/superadmin | Admin grants admin rights to user
[**adminListsAllAdmins**](AdminApi.md#adminListsAllAdmins) | **GET** /api/admin/admins | Admin lists all admins
[**adminListsAllProjects**](AdminApi.md#adminListsAllProjects) | **GET** /api/admin/projects | Admin lists all projects
[**adminListsAllRunningJobs**](AdminApi.md#adminListsAllRunningJobs) | **GET** /api/admin/jobs/running | Admin lists all running jobs
[**adminListsAllUsers**](AdminApi.md#adminListsAllUsers) | **GET** /api/admin/users | Admin lists all users
[**adminListsOpenUserSignups**](AdminApi.md#adminListsOpenUserSignups) | **GET** /api/admin/signups | Admin lists open user signups
[**adminListsStatusInformation**](AdminApi.md#adminListsStatusInformation) | **GET** /api/admin/status | Admin lists status information
[**adminRestartsJob**](AdminApi.md#adminRestartsJob) | **POST** /api/admin/jobs/restart/{jobUUID} | Admin restarts a job
[**adminRestartsJobHard**](AdminApi.md#adminRestartsJobHard) | **POST** /api/admin/jobs/restart-hard/{jobUUID} | Admin restarts a job (hard)
[**adminRevokesAdminRightsFromAdmin**](AdminApi.md#adminRevokesAdminRightsFromAdmin) | **POST** /api/admin/user/{userId}/revoke/superadmin | Admin revokes admin rights from an admin
[**adminShowsProjectDetails**](AdminApi.md#adminShowsProjectDetails) | **GET** /api/admin/project/{projectId} | Admin shows project details
[**adminShowsScanLogsForProject**](AdminApi.md#adminShowsScanLogsForProject) | **GET** /api/admin/project/{projectId}/scan/logs | Admin shows scan logs for project
[**adminShowsUserDetails**](AdminApi.md#adminShowsUserDetails) | **GET** /api/admin/user/{userId} | Admin shows user details
[**adminTriggersRefreshOfSchedulerStatus**](AdminApi.md#adminTriggersRefreshOfSchedulerStatus) | **POST** /api/admin/scheduler/status/refresh | Admin get scheduler status
[**adminUnassignsExecutionProfileFromProject**](AdminApi.md#adminUnassignsExecutionProfileFromProject) | **DELETE** /api/admin/config/execution/profile/{profileId}/project/{projectId} | Admin unassigns execution profile from project
[**adminUnassignsUserFromProject**](AdminApi.md#adminUnassignsUserFromProject) | **DELETE** /api/admin/project/{userId}/membership/{projectId} | Admin unassigns user from project
[**adminUpdatesExecutionProfile**](AdminApi.md#adminUpdatesExecutionProfile) | **PUT** /api/admin/config/execution/profile/{profileId} | Admin updates execution profile
[**adminUpdatesExecutorConfig**](AdminApi.md#adminUpdatesExecutorConfig) | **PUT** /api/admin/config/executor/{uuid} | Admin updates executor configuration setup
[**adminUpdatesMappingConfiguration**](AdminApi.md#adminUpdatesMappingConfiguration) | **PUT** /api/admin/config/mapping/{mappingId} | Admin updates mapping configuration
[**updateProjectMetaData**](AdminApi.md#updateProjectMetaData) | **POST** /api/admin/project/{projectId}/metadata | Update project metadata
[**updateProjectWhitelist**](AdminApi.md#updateProjectWhitelist) | **POST** /api/admin/project/{projectId}/whitelist | Update project whitelist


<a name="adminAcceptsSignup"></a>
# **adminAcceptsSignup**
> adminAcceptsSignup(userId)

Admin applies self registration

In this usecase the administrator will accept the self registration done by an user.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The userId of the signup which shall be accepted
    try {
      apiInstance.adminAcceptsSignup(userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminAcceptsSignup");
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
 **userId** | **String**| The userId of the signup which shall be accepted |

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
**201** | 201 |  -  |

<a name="adminAssignsExecutionProfileToProject"></a>
# **adminAssignsExecutionProfileToProject**
> adminAssignsExecutionProfileToProject(profileId, projectId)

Admin assigns execution profile to project

An administrator assigns an execution profile to an existing project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String profileId = "profileId_example"; // String | The profile id
    String projectId = "projectId_example"; // String | The project id 
    try {
      apiInstance.adminAssignsExecutionProfileToProject(profileId, projectId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminAssignsExecutionProfileToProject");
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
 **profileId** | **String**| The profile id |
 **projectId** | **String**| The project id  |

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
**201** | 201 |  -  |

<a name="adminAssignsUserToProject"></a>
# **adminAssignsUserToProject**
> adminAssignsUserToProject(userId, projectId)

Admin assigns user to project

An administrator assigns an user to an existing sechub project.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The user id of the user to assign to project
    String projectId = "projectId_example"; // String | The id for project
    try {
      apiInstance.adminAssignsUserToProject(userId, projectId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminAssignsUserToProject");
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
 **userId** | **String**| The user id of the user to assign to project |
 **projectId** | **String**| The id for project |

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

<a name="adminCancelsJob"></a>
# **adminCancelsJob**
> adminCancelsJob(jobUUID)

Admin cancels a job

Administrator does cancel a job by its Job UUID

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String jobUUID = "jobUUID_example"; // String | The job UUID
    try {
      apiInstance.adminCancelsJob(jobUUID);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminCancelsJob");
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
 **jobUUID** | **String**| The job UUID |

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

<a name="adminChangesProjectOwner"></a>
# **adminChangesProjectOwner**
> adminChangesProjectOwner(userId, projectId)

Admin changes owner of a project

An administrator changes the owner of an existing sechub project.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The user id of the user to assign to project as the owner
    String projectId = "projectId_example"; // String | The id for project
    try {
      apiInstance.adminChangesProjectOwner(userId, projectId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminChangesProjectOwner");
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
 **userId** | **String**| The user id of the user to assign to project as the owner |
 **projectId** | **String**| The id for project |

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

<a name="adminChecksServerVersion"></a>
# **adminChecksServerVersion**
> Object adminChecksServerVersion()

Admin checks server version

An administrator checks the current SecHub server version. Only administrators are able to check the server version, because knowing the exact server version makes it easier for penetration tester or attacker to attack the system.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      Object result = apiInstance.adminChecksServerVersion();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminChecksServerVersion");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**Object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain;charset=UTF-8

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminCreatesExecutionProfile"></a>
# **adminCreatesExecutionProfile**
> adminCreatesExecutionProfile(profileId, executionProfileCreate)

Admin creates an execution proflie

An administrator creates an execution profile

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String profileId = "profileId_example"; // String | The profile id
    ExecutionProfileCreate executionProfileCreate = {"description":"a short description for profile","configurations":[],"projectIds":[],"enabled":false}; // ExecutionProfileCreate | 
    try {
      apiInstance.adminCreatesExecutionProfile(profileId, executionProfileCreate);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminCreatesExecutionProfile");
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
 **profileId** | **String**| The profile id |
 **executionProfileCreate** | [**ExecutionProfileCreate**](ExecutionProfileCreate.md)|  | [optional]

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
**201** | 201 |  -  |

<a name="adminCreatesExecutorConfiguration"></a>
# **adminCreatesExecutorConfiguration**
> Object adminCreatesExecutorConfiguration(executorConfiguration)

Admin creates an executor configuration

An administrator creates an executor a new configuration entry.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    ExecutorConfiguration executorConfiguration = {"name":"PDS gosec config 1","productIdentifier":"PDS_CODESCAN","executorVersion":1,"enabled":false,"setup":{"baseURL":"https://productXYZ.example.com","credentials":{"user":"env:EXAMPLE_USENAME","password":"env:EXAMPLE_PASSWORD"},"jobParameters":[{"key":"example.key1","value":"A value"},{"key":"example.key2","value":"Another value"}]}}; // ExecutorConfiguration | 
    try {
      Object result = apiInstance.adminCreatesExecutorConfiguration(executorConfiguration);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminCreatesExecutorConfiguration");
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
 **executorConfiguration** | [**ExecutorConfiguration**](ExecutorConfiguration.md)|  | [optional]

### Return type

**Object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json;charset=UTF-8
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**201** | 201 |  -  |

<a name="adminCreatesProject"></a>
# **adminCreatesProject**
> adminCreatesProject(project)

Admin creates a project

Admin creates a project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    Project project = {"apiVersion":"1.0", "name":"projectId", "description":"A description of the project.", "owner":"ownerName1", "whiteList":{"uris":["192.168.1.1","https://my.special.server.com/myapp1/"]}, "metaData":{"key1":"value1", "key2":"value2"}}; // Project | 
    try {
      apiInstance.adminCreatesProject(project);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminCreatesProject");
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
 **project** | [**Project**](Project.md)|  | [optional]

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
**201** | 201 |  -  |

<a name="adminDeleteProject"></a>
# **adminDeleteProject**
> adminDeleteProject(projectId)

Admin deletes a project

Admin deletes a project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String projectId = "projectId_example"; // String | The id for project to delete
    try {
      apiInstance.adminDeleteProject(projectId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDeleteProject");
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
 **projectId** | **String**| The id for project to delete |

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

<a name="adminDeletesExecutionProfile"></a>
# **adminDeletesExecutionProfile**
> adminDeletesExecutionProfile(profileId)

Admin deletes execution profile

An administrator deletes execution profile

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String profileId = "profileId_example"; // String | The profile id
    try {
      apiInstance.adminDeletesExecutionProfile(profileId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDeletesExecutionProfile");
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
 **profileId** | **String**| The profile id |

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

<a name="adminDeletesExecutorConfiguration"></a>
# **adminDeletesExecutorConfiguration**
> adminDeletesExecutorConfiguration(uuid)

Admin deletes executor configuration

An administrator deletes an executor by removing the configuration entry identified by its uuid

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String uuid = "uuid_example"; // String | The configuration uuid
    try {
      apiInstance.adminDeletesExecutorConfiguration(uuid);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDeletesExecutorConfiguration");
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
 **uuid** | **String**| The configuration uuid |

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

<a name="adminDeletesSignup"></a>
# **adminDeletesSignup**
> adminDeletesSignup(userId)

Admin deletes user signup

In this usecase the administrator will not accept the self registration done by an user but delete the entry.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The userId of the signup which shall be deleted
    try {
      apiInstance.adminDeletesSignup(userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDeletesSignup");
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
 **userId** | **String**| The userId of the signup which shall be deleted |

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

<a name="adminDeletesUser"></a>
# **adminDeletesUser**
> adminDeletesUser(userId)

Admin deletes a user

Admin deletes a user

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The userId of the user who shall be deleted
    try {
      apiInstance.adminDeletesUser(userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDeletesUser");
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
 **userId** | **String**| The userId of the user who shall be deleted |

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

<a name="adminDisablesSchedulerJobProcessing"></a>
# **adminDisablesSchedulerJobProcessing**
> adminDisablesSchedulerJobProcessing()

Admin disables job processing in scheduler

An administrator disables scheduler job processing. This can be a preparation for system wide update - when scheduling is stoped, user can ask for new SecHub Jobs etc. But as long as scheduler is stopped nothing is executed - so JVMs/PODs can be updated in cluster

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      apiInstance.adminDisablesSchedulerJobProcessing();
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDisablesSchedulerJobProcessing");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

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
**202** | 202 |  -  |

<a name="adminDownloadsFullScanDataForJob"></a>
# **adminDownloadsFullScanDataForJob**
> Object adminDownloadsFullScanDataForJob(jobUUID)

Admin downloads all details about a scan job

An administrator downloads a ZIP file containing full details of a scan. Main reason for this use case is for debugging when there are problems with security products. Another reason is for developers to adopt new security products easier.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String jobUUID = "jobUUID_example"; // String | The job UUID
    try {
      Object result = apiInstance.adminDownloadsFullScanDataForJob(jobUUID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminDownloadsFullScanDataForJob");
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
 **jobUUID** | **String**| The job UUID |

### Return type

**Object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/zip

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminEnablesSchedulerJobProcessing"></a>
# **adminEnablesSchedulerJobProcessing**
> adminEnablesSchedulerJobProcessing()

Admin enables scheduler job processing

An administrator starts scheduler job processing. This can be a necessary step after a system wide update where processing of jobs was stoped before.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      apiInstance.adminEnablesSchedulerJobProcessing();
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminEnablesSchedulerJobProcessing");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

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
**202** | 202 |  -  |

<a name="adminFetchesExecutionProfile"></a>
# **adminFetchesExecutionProfile**
> ExecutionProfileFetch adminFetchesExecutionProfile(profileId)

Admin fetches execution profile

An administrator fetches details about an execution profile

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String profileId = "profileId_example"; // String | The profile id
    try {
      ExecutionProfileFetch result = apiInstance.adminFetchesExecutionProfile(profileId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminFetchesExecutionProfile");
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
 **profileId** | **String**| The profile id |

### Return type

[**ExecutionProfileFetch**](ExecutionProfileFetch.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminFetchesExecutionProfileList"></a>
# **adminFetchesExecutionProfileList**
> ListOfExecutionProfiles adminFetchesExecutionProfileList()

Admin fetches execution proflie list

An administrator fetches execution profile list

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      ListOfExecutionProfiles result = apiInstance.adminFetchesExecutionProfileList();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminFetchesExecutionProfileList");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**ListOfExecutionProfiles**](ListOfExecutionProfiles.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminFetchesExecutorConfiguration"></a>
# **adminFetchesExecutorConfiguration**
> ExecutorConfigurationWithUUID adminFetchesExecutorConfiguration(uuid)

Admin fetches executor configuration

An administrator fetches one explicit executor configuration by its uuid.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String uuid = "uuid_example"; // String | The configuration uuid
    try {
      ExecutorConfigurationWithUUID result = apiInstance.adminFetchesExecutorConfiguration(uuid);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminFetchesExecutorConfiguration");
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
 **uuid** | **String**| The configuration uuid |

### Return type

[**ExecutorConfigurationWithUUID**](ExecutorConfigurationWithUUID.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminFetchesExecutorConfigurationList"></a>
# **adminFetchesExecutorConfigurationList**
> ListOfExecutorConfigurations adminFetchesExecutorConfigurationList()

Admin fetches executor configuration list

An administrator fetches executor configuration list which contains all executor configurations

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      ListOfExecutorConfigurations result = apiInstance.adminFetchesExecutorConfigurationList();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminFetchesExecutorConfigurationList");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**ListOfExecutorConfigurations**](ListOfExecutorConfigurations.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminFetchesMappingConfiguration"></a>
# **adminFetchesMappingConfiguration**
> MappingConfiguration adminFetchesMappingConfiguration(mappingId)

Admin fetches mapping configuration

An administrator fetches mapping configuration by its ID.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String mappingId = "mappingId_example"; // String | The mapping Id
    try {
      MappingConfiguration result = apiInstance.adminFetchesMappingConfiguration(mappingId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminFetchesMappingConfiguration");
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
 **mappingId** | **String**| The mapping Id |

### Return type

[**MappingConfiguration**](MappingConfiguration.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminGrantsAdminRightsToUser"></a>
# **adminGrantsAdminRightsToUser**
> adminGrantsAdminRightsToUser(userId)

Admin grants admin rights to user

An administrator grants admin rights to another user. So this user will become also an administrator.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The userId of the user who becomes admin
    try {
      apiInstance.adminGrantsAdminRightsToUser(userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminGrantsAdminRightsToUser");
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
 **userId** | **String**| The userId of the user who becomes admin |

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

<a name="adminListsAllAdmins"></a>
# **adminListsAllAdmins**
> List&lt;Object&gt; adminListsAllAdmins()

Admin lists all admins

An administrator downloads a json file containing all names of SecHub admins

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      List<Object> result = apiInstance.adminListsAllAdmins();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminListsAllAdmins");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminListsAllProjects"></a>
# **adminListsAllProjects**
> List&lt;Object&gt; adminListsAllProjects()

Admin lists all projects

An administrator downloads a json file containing all project ids

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      List<Object> result = apiInstance.adminListsAllProjects();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminListsAllProjects");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminListsAllRunningJobs"></a>
# **adminListsAllRunningJobs**
> List&lt;Object&gt; adminListsAllRunningJobs()

Admin lists all running jobs

Admin lists all running jobs

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      List<Object> result = apiInstance.adminListsAllRunningJobs();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminListsAllRunningJobs");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminListsAllUsers"></a>
# **adminListsAllUsers**
> List&lt;Object&gt; adminListsAllUsers()

Admin lists all users

An administrator downloads a json file containing all user ids

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      List<Object> result = apiInstance.adminListsAllUsers();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminListsAllUsers");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminListsOpenUserSignups"></a>
# **adminListsOpenUserSignups**
> List&lt;Object&gt; adminListsOpenUserSignups()

Admin lists open user signups

In this usecase the administrator will list the currently unapplied user self registrations/signups.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      List<Object> result = apiInstance.adminListsOpenUserSignups();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminListsOpenUserSignups");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminListsStatusInformation"></a>
# **adminListsStatusInformation**
> List&lt;Object&gt; adminListsStatusInformation()

Admin lists status information

An administrator fetches current known status information about sechub

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      List<Object> result = apiInstance.adminListsStatusInformation();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminListsStatusInformation");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminRestartsJob"></a>
# **adminRestartsJob**
> adminRestartsJob(jobUUID)

Admin restarts a job

Admin restarts a job

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String jobUUID = "jobUUID_example"; // String | The job UUID
    try {
      apiInstance.adminRestartsJob(jobUUID);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminRestartsJob");
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
 **jobUUID** | **String**| The job UUID |

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

<a name="adminRestartsJobHard"></a>
# **adminRestartsJobHard**
> adminRestartsJobHard(jobUUID)

Admin restarts a job (hard)

Admin restarts a job (hard)

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String jobUUID = "jobUUID_example"; // String | The job UUID
    try {
      apiInstance.adminRestartsJobHard(jobUUID);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminRestartsJobHard");
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
 **jobUUID** | **String**| The job UUID |

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

<a name="adminRevokesAdminRightsFromAdmin"></a>
# **adminRevokesAdminRightsFromAdmin**
> adminRevokesAdminRightsFromAdmin(userId)

Admin revokes admin rights from an admin

An administrator revokes existing admin rights from another administrator.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The userId of the user who becomes admin
    try {
      apiInstance.adminRevokesAdminRightsFromAdmin(userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminRevokesAdminRightsFromAdmin");
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
 **userId** | **String**| The userId of the user who becomes admin |

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

<a name="adminShowsProjectDetails"></a>
# **adminShowsProjectDetails**
> ProjectDetails adminShowsProjectDetails(projectId)

Admin shows project details

An administrator downloads a json file containing json with project details

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String projectId = "projectId_example"; // String | The id for project to show details for
    try {
      ProjectDetails result = apiInstance.adminShowsProjectDetails(projectId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminShowsProjectDetails");
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
 **projectId** | **String**| The id for project to show details for |

### Return type

[**ProjectDetails**](ProjectDetails.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminShowsScanLogsForProject"></a>
# **adminShowsScanLogsForProject**
> List&lt;Object&gt; adminShowsScanLogsForProject(projectId)

Admin shows scan logs for project

An admin downloads a json file containing log for scans of project

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String projectId = "projectId_example"; // String | The project Id
    try {
      List<Object> result = apiInstance.adminShowsScanLogsForProject(projectId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminShowsScanLogsForProject");
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

### Return type

**List&lt;Object&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminShowsUserDetails"></a>
# **adminShowsUserDetails**
> UserDetails adminShowsUserDetails(userId)

Admin shows user details

An administrator downloads a json file containing json containing user details

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The user id of user to show details for
    try {
      UserDetails result = apiInstance.adminShowsUserDetails(userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminShowsUserDetails");
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
 **userId** | **String**| The user id of user to show details for |

### Return type

[**UserDetails**](UserDetails.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | 200 |  -  |

<a name="adminTriggersRefreshOfSchedulerStatus"></a>
# **adminTriggersRefreshOfSchedulerStatus**
> adminTriggersRefreshOfSchedulerStatus()

Admin get scheduler status

An administrator wants to update information about scheduler status

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      apiInstance.adminTriggersRefreshOfSchedulerStatus();
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminTriggersRefreshOfSchedulerStatus");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

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
**202** | 202 |  -  |

<a name="adminUnassignsExecutionProfileFromProject"></a>
# **adminUnassignsExecutionProfileFromProject**
> adminUnassignsExecutionProfileFromProject(profileId, projectId)

Admin unassigns execution profile from project

An administrator unassigns an execution profile from a projects.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String profileId = "profileId_example"; // String | The profile id
    String projectId = "projectId_example"; // String | The project id 
    try {
      apiInstance.adminUnassignsExecutionProfileFromProject(profileId, projectId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminUnassignsExecutionProfileFromProject");
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
 **profileId** | **String**| The profile id |
 **projectId** | **String**| The project id  |

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

<a name="adminUnassignsUserFromProject"></a>
# **adminUnassignsUserFromProject**
> adminUnassignsUserFromProject(userId, projectId)

Admin unassigns user from project

An administrator unassigns an user from a sechub project.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String userId = "userId_example"; // String | The user id of the user to unassign from project
    String projectId = "projectId_example"; // String | The id for project
    try {
      apiInstance.adminUnassignsUserFromProject(userId, projectId);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminUnassignsUserFromProject");
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
 **userId** | **String**| The user id of the user to unassign from project |
 **projectId** | **String**| The id for project |

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

<a name="adminUpdatesExecutionProfile"></a>
# **adminUpdatesExecutionProfile**
> adminUpdatesExecutionProfile(profileId, executionProfileUpdate)

Admin updates execution profile

An administrator updateds dedicated execution profile

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String profileId = "profileId_example"; // String | The profile id
    ExecutionProfileUpdate executionProfileUpdate = {"description":"changed description","configurations":[{"uuid":"92252018-acc6-4dd2-8b80-766156f5cf52","executorVersion":0,"enabled":false,"setup":{"credentials":{},"jobParameters":[]}}],"enabled":true}; // ExecutionProfileUpdate | 
    try {
      apiInstance.adminUpdatesExecutionProfile(profileId, executionProfileUpdate);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminUpdatesExecutionProfile");
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
 **profileId** | **String**| The profile id |
 **executionProfileUpdate** | [**ExecutionProfileUpdate**](ExecutionProfileUpdate.md)|  | [optional]

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

<a name="adminUpdatesExecutorConfig"></a>
# **adminUpdatesExecutorConfig**
> adminUpdatesExecutorConfig(uuid, executorConfiguration)

Admin updates executor configuration setup

An administrator updateds dedicated executor configuration. The update does change description, enabled state and also used executors, but Will NOT change any associations between profile and projects.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String uuid = "uuid_example"; // String | The configuration uuid
    ExecutorConfiguration executorConfiguration = {"name":"New name","productIdentifier":"PDS_CODESCAN","executorVersion":1,"enabled":false,"setup":{"baseURL":"https://productNew.example.com","credentials":{"user":"env:EXAMPLE_NEW_USENAME","password":"env:EXAMPLE_NEW_PASSWORD"},"jobParameters":[{"key":"example.key1","value":"A value but changed. Remark: the other parameter (example.key2) has been removed by this call"}]}}; // ExecutorConfiguration | 
    try {
      apiInstance.adminUpdatesExecutorConfig(uuid, executorConfiguration);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminUpdatesExecutorConfig");
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
 **uuid** | **String**| The configuration uuid |
 **executorConfiguration** | [**ExecutorConfiguration**](ExecutorConfiguration.md)|  | [optional]

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

<a name="adminUpdatesMappingConfiguration"></a>
# **adminUpdatesMappingConfiguration**
> adminUpdatesMappingConfiguration(mappingId, mappingConfiguration)

Admin updates mapping configuration

An administrator changes mapping configuration. Mappings represents a generic mechanism to replace a given string, matched by configured regular expression pattern with a replacement string. Some of the mappings are used for adapter behaviour.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String mappingId = "mappingId_example"; // String | The mappingID, identifiying which mapping shall be updated
    MappingConfiguration mappingConfiguration = {"entries":[{"pattern":"testproject_*","replacement":"8be4e3d4-6b53-4636-b65a-949a9ebdf6b9","comment":"testproject-team"},{"pattern":".*","replacement":"3be4e3d2-2b55-2336-b65a-949b9ebdf6b9","comment":"default-team"}]}; // MappingConfiguration | 
    try {
      apiInstance.adminUpdatesMappingConfiguration(mappingId, mappingConfiguration);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#adminUpdatesMappingConfiguration");
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
 **mappingId** | **String**| The mappingID, identifiying which mapping shall be updated |
 **mappingConfiguration** | [**MappingConfiguration**](MappingConfiguration.md)|  | [optional]

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

<a name="updateProjectMetaData"></a>
# **updateProjectMetaData**
> updateProjectMetaData(projectId, projectWhitelist)

Update project metadata

Update project metadata

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String projectId = "projectId_example"; // String | The id of the project for which metadata shall be updated
    ProjectWhitelist projectWhitelist = {"apiVersion":"1.0", "metaData":{"key1":"value1"}}; // ProjectWhitelist | 
    try {
      apiInstance.updateProjectMetaData(projectId, projectWhitelist);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#updateProjectMetaData");
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
 **projectId** | **String**| The id of the project for which metadata shall be updated |
 **projectWhitelist** | [**ProjectWhitelist**](ProjectWhitelist.md)|  | [optional]

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

<a name="updateProjectWhitelist"></a>
# **updateProjectWhitelist**
> updateProjectWhitelist(projectId, projectWhitelist)

Update project whitelist

Update project whitelist

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AdminApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AdminApi apiInstance = new AdminApi(defaultClient);
    String projectId = "projectId_example"; // String | The id of the project for which whitelist shall be updated
    ProjectWhitelist projectWhitelist = {"apiVersion":"1.0", "whiteList":{"uris":["192.168.1.1","https://my.special.server.com/myapp1/"]}}; // ProjectWhitelist | 
    try {
      apiInstance.updateProjectWhitelist(projectId, projectWhitelist);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#updateProjectWhitelist");
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
 **projectId** | **String**| The id of the project for which whitelist shall be updated |
 **projectWhitelist** | [**ProjectWhitelist**](ProjectWhitelist.md)|  | [optional]

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

