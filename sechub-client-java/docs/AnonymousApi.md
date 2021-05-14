# AnonymousApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**anonymousCheckAliveGet**](AnonymousApi.md#anonymousCheckAliveGet) | **GET** /api/anonymous/check/alive | Check if the server is alive and running.
[**anonymousCheckAliveHead**](AnonymousApi.md#anonymousCheckAliveHead) | **HEAD** /api/anonymous/check/alive | Check if the server is alive and running.
[**userClicksLinkToGetNewAPIToken**](AnonymousApi.md#userClicksLinkToGetNewAPIToken) | **GET** /api/anonymous/apitoken/{oneTimeToken} | User clicks link to get new api token
[**userRequestsNewApiToken**](AnonymousApi.md#userRequestsNewApiToken) | **POST** /api/anonymous/refresh/apitoken/{emailAddress} | User requests new API token
[**userSignup**](AnonymousApi.md#userSignup) | **POST** /api/anonymous/signup | User self registration


<a name="anonymousCheckAliveGet"></a>
# **anonymousCheckAliveGet**
> anonymousCheckAliveGet()

Check if the server is alive and running.

An anonymous user or system wants to know if the server is alive and running.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AnonymousApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AnonymousApi apiInstance = new AnonymousApi(defaultClient);
    try {
      apiInstance.anonymousCheckAliveGet();
    } catch (ApiException e) {
      System.err.println("Exception when calling AnonymousApi#anonymousCheckAliveGet");
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
**200** | 200 |  -  |

<a name="anonymousCheckAliveHead"></a>
# **anonymousCheckAliveHead**
> anonymousCheckAliveHead()

Check if the server is alive and running.

An anonymous user or system wants to know if the server is alive and running.

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AnonymousApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AnonymousApi apiInstance = new AnonymousApi(defaultClient);
    try {
      apiInstance.anonymousCheckAliveHead();
    } catch (ApiException e) {
      System.err.println("Exception when calling AnonymousApi#anonymousCheckAliveHead");
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
**200** | 200 |  -  |

<a name="userClicksLinkToGetNewAPIToken"></a>
# **userClicksLinkToGetNewAPIToken**
> userClicksLinkToGetNewAPIToken(oneTimeToken)

User clicks link to get new api token

User clicks link to get new api token

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AnonymousApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AnonymousApi apiInstance = new AnonymousApi(defaultClient);
    String oneTimeToken = "oneTimeToken_example"; // String | A one time token the user has got by a previous mail from sechub server
    try {
      apiInstance.userClicksLinkToGetNewAPIToken(oneTimeToken);
    } catch (ApiException e) {
      System.err.println("Exception when calling AnonymousApi#userClicksLinkToGetNewAPIToken");
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
 **oneTimeToken** | **String**| A one time token the user has got by a previous mail from sechub server |

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

<a name="userRequestsNewApiToken"></a>
# **userRequestsNewApiToken**
> userRequestsNewApiToken(emailAddress)

User requests new API token

User requests new API token

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AnonymousApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AnonymousApi apiInstance = new AnonymousApi(defaultClient);
    String emailAddress = "emailAddress_example"; // String | Email address for user where api token shall be refreshed.
    try {
      apiInstance.userRequestsNewApiToken(emailAddress);
    } catch (ApiException e) {
      System.err.println("Exception when calling AnonymousApi#userRequestsNewApiToken");
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
 **emailAddress** | **String**| Email address for user where api token shall be refreshed. |

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

<a name="userSignup"></a>
# **userSignup**
> userSignup(userSignup)

User self registration

User self registration

### Example
```java
// Import classes:
import com.daimler.sechub.client.java.api.ApiClient;
import com.daimler.sechub.client.java.api.ApiException;
import com.daimler.sechub.client.java.api.Configuration;
import com.daimler.sechub.client.java.api.models.*;
import com.daimler.sechub.client.java.api.AnonymousApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://localhost");

    AnonymousApi apiInstance = new AnonymousApi(defaultClient);
    UserSignup userSignup = {"apiVersion":"1.0","userId":"valid_userid","emailAdress":"valid_mailadress@test.com"}; // UserSignup | 
    try {
      apiInstance.userSignup(userSignup);
    } catch (ApiException e) {
      System.err.println("Exception when calling AnonymousApi#userSignup");
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
 **userSignup** | [**UserSignup**](UserSignup.md)|  | [optional]

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

