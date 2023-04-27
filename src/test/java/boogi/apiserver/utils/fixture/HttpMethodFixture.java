package boogi.apiserver.utils.fixture;

import static boogi.apiserver.global.constant.HeaderConst.AUTH_TOKEN;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.MediaType;


public class HttpMethodFixture {

    private static final String REQUEST_URI_PREFIX = "/api";

    public static ExtractableResponse<Response> httpGet(String path) {
        return RestAssured
                .given().log().all()
                .when().get(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpGet(String path, String token) {
        return RestAssured
                .given().log().all()
                .header(AUTH_TOKEN, token)
                .when().get(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpPost(Object postRequest, String path) {
        return RestAssured
                .given().log().all()
                .body(postRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpPost(String path) {
        return RestAssured
                .given().log().all()
                .when().post(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpPost(Object requestBody, String path, String token) {
        return RestAssured
                .given().log().all()
                .header(AUTH_TOKEN, token)
                .body(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpPut(Object requestBody, String path, String token) {
        return RestAssured
                .given().log().all()
                .header(AUTH_TOKEN, token)
                .body(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpPut(String path, String token) {
        return RestAssured
                .given().log().all()
                .header(AUTH_TOKEN, token)
                .when().put(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpPatch(Object requestBody, String path, String token) {
        return RestAssured
                .given().log().all()
                .header(AUTH_TOKEN, token)
                .body(requestBody)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().patch(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> httpDelete(String path, String token) {
        return RestAssured
                .given().log().all()
                .header(AUTH_TOKEN, token)
                .when().delete(REQUEST_URI_PREFIX + path)
                .then().log().all()
                .extract();
    }

    public static String getExceptionMessage(ExtractableResponse<Response> response) {
        return response.jsonPath().getString("message");
    }
}
